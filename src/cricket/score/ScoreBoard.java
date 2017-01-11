package cricket.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cricket.Player;

public class ScoreBoard {

	private static final BigDecimal BATTING_BONUS_FACTOR = BigDecimal.valueOf(0.1);
	private static final BigDecimal BOWLING_BONUS_FACTOR = BigDecimal.valueOf(0.1);
	private static final BigDecimal NON_ASSIST_BOWLING_POINTS = new BigDecimal(25);
	private static final BigDecimal ASSIST_BOWLING_POINTS = new BigDecimal(12.5);
	
	private final List<Score> scores;
	private final Map<String, Set<Player>> teamComposition = new HashMap<>();

	public ScoreBoard(List<Score> scores) {
		this.scores = new ArrayList<>(scores);
		this.scores.forEach(score -> {
			Set<Player> battingTeamMates = teamComposition.get(score.getBattingTeamName());
			if(battingTeamMates == null) {
				battingTeamMates = new HashSet<>();
				teamComposition.put(score.getBattingTeamName(), battingTeamMates);
			}
			battingTeamMates.add(score.getBatsman());
			battingTeamMates.add(score.getNonStriker());
			
			Set<Player> bowlingTeamMates = teamComposition.get(score.getBowlingTeamName());
			if(bowlingTeamMates == null) {
				bowlingTeamMates  = new HashSet<>();
				teamComposition.put(score.getBowlingTeamName(), bowlingTeamMates);
			}
			bowlingTeamMates.add(score.getBowler());
			if(score.getAssistingPlayer().isPresent()) {
				bowlingTeamMates.add(score.getAssistingPlayer().get());
			}			
		});
		
	}
	
	
	Set<Player> getTeamPlayers(String teamName) {
		return Collections.unmodifiableSet(teamComposition.get(teamName));
	}
	
	public Set<Player> getManOfMatch() {
		
		Comparator<Player> pointsComparator = (p1, p2) -> calculateMatchPoints(p2).compareTo(calculateMatchPoints(p1));
		
		List<Player> rankings = scores.stream()
			.flatMap(this::participatingPlayers)
			.distinct()
			.sorted(pointsComparator)
			.collect(Collectors.toList());
		
//		System.out.println(rankings);
		
		Player firstOne = rankings.get(0);
	
		// Find all players having same points as the top player.
		// There could be more than one player who scores maximum points.
		
		Set<Player> topRankingPlayers = rankings
			.stream()
			.filter(p -> pointsComparator.compare(firstOne, p) == 0)
			.collect(Collectors.toSet());
		
		return topRankingPlayers;
	}
	
	private Stream<Player> participatingPlayers(Score score) {
		return  score.getPlayers().stream();
	}

	public BigDecimal calculateMatchPoints(Player player) {
		
		return calculateBasePoints(player)
				.add(calculateBonusPoints(player));
	}

	public BigDecimal calculateBasePoints(Player player) {
		
		int battingPoints = calculateBaseBattingPoints(player);
		
		return new BigDecimal(battingPoints) 
				.add(calculateBaseBowlingPoints(player)) 
				.add(calculateBaseFieldingPoints(player));
	}
	
	public BigDecimal calculateBonusPoints(Player player) {
		
		return calculateBonusBattingPoints(player)
				.add(calculateBonusBowlingPoints(player));
	}
	
	public int calculateBaseBattingPoints(Player player) {
		
		return scores.stream()
				.filter(score -> player.equals(score.getBatsman()))
				.mapToInt(Score::getRuns)
				.sum();
	}
	
	public BigDecimal calculateBaseBowlingPoints(Player player) {
		
		return scores.stream()
				.filter(score -> player.equals(score.getBowler()))
				.filter(Score::isDismissalDelivery)
				.map(score -> {
					
					if(!score.getAssistingPlayer().isPresent() || player.equals(score.getAssistingPlayer().get())) {
						return NON_ASSIST_BOWLING_POINTS;
					} else {
						return ASSIST_BOWLING_POINTS;
					}
				})
				.reduce(new BigDecimal(0), (a,b) -> a.add(b));
	}

	public BigDecimal calculateBaseFieldingPoints(Player player) {
		return scores.stream()
			.filter(score -> score.getAssistingPlayer().isPresent())
			.filter(score -> player.equals(score.getAssistingPlayer().get()))
			.map(score ->{
				if(score.getAssistingPlayer().get().equals(score.getBowler())) {
					return BigDecimal.ZERO; 
				} else {
					return ASSIST_BOWLING_POINTS;
				}
			})
			.reduce(new BigDecimal(0), (a,b) -> a.add(b));
	}

	public BigDecimal calculateBonusBattingPoints(Player player) {
		
		if(getBallsFacedByPlayer(player) == 0) {
			return BigDecimal.ZERO;
		}
		
		BigDecimal teamStrikeRate = getTeamStrikeRate(getTeam(player));
		BigDecimal playerStrikeRate = getPlayerStrikeRate(player);
		
		BigDecimal diff = playerStrikeRate.subtract(teamStrikeRate);
		
		if(isPlayerStrikeRateMarkedlyDifferent(diff, teamStrikeRate)) {
			BigDecimal bonusPoints = new BigDecimal(calculateBaseBattingPoints(player)).multiply(BATTING_BONUS_FACTOR);
			
			if(diff.signum() == 1) { // player strike rate is more than team strike rate
				return bonusPoints;
			} else {
				return bonusPoints.negate();
			}
		} else {
			return BigDecimal.ZERO;
		}
	}


	private Boolean isPlayerStrikeRateMarkedlyDifferent(BigDecimal diff, BigDecimal teamStrikeRate) {
		return isGreater(diff.abs(), teamStrikeRate.multiply(BATTING_BONUS_FACTOR).abs());
	}
	
	String getTeam(Player player) {
		return teamComposition.entrySet()
		.stream()
		.filter(e -> e.getValue().contains(player))
		.map(Map.Entry::getKey)
		.findFirst()
		.orElseThrow(() -> new RuntimeException("No team found for " + player));
	}
	
	private Boolean isGreater(BigDecimal a, BigDecimal b) {
		BigDecimal diff = a.subtract(b);
		if(diff.signum() == 0) {
			return null;
		} else if(diff.signum() == 1) {
			return true;
		} else {
			return false;
		}
	}

	public BigDecimal getPlayerStrikeRate(Player player) {
		
		int playerRuns = getPlayerRuns(player);
		
		long ballsFaced = getBallsFacedByPlayer(player);
		
		if(ballsFaced == 0) {
			return BigDecimal.ZERO;
		}
		
		return new BigDecimal(playerRuns).divide(new BigDecimal(ballsFaced), 2, RoundingMode.HALF_UP);
	}

	public long getBallsFacedByPlayer(Player player) {
		return scores.stream()
				.filter(Score::isNonExtraDelivery)
				.filter(score -> player.equals(score.getBatsman()))				
				.count();
	}

	public int getPlayerRuns(Player player) {
		return scores.stream()	
				.filter(score -> player.equals(score.getBatsman()))
				.mapToInt(score -> score.getRuns())
				.sum();
	}

	public BigDecimal getTeamStrikeRate(String teamName) {
		
		int totalRuns = getTeamRuns(teamName);
			
		long totalBalls = getTeamBalls(teamName);
		
		return new BigDecimal(totalRuns).divide(new BigDecimal(totalBalls), 2, RoundingMode.HALF_UP);
	}
	
	public BigDecimal getTeamEconomyRate(String teamName) {
		
		int totalRuns = getTeamRunsGiven(teamName);
			
		long totalBalls = getTeamBallsBowled(teamName);
		
		return new BigDecimal(totalRuns).divide(new BigDecimal(totalBalls), 2, RoundingMode.HALF_UP);
	}

	public long getTeamBalls(String teamName) {
		return scores.stream()
				.filter(score -> score.getBattingTeamName().equals(teamName))
				.filter(Score::isNonExtraDelivery)
				.count();
	}
	
	public long getTeamBallsBowled(String teamName) {
		return scores.stream()
				.filter(score -> score.getBowlingTeamName().equals(teamName))
				.filter(Score::isNonExtraDelivery)
				.count();
	}

	public int getTeamRuns(String teamName) {
		return scores.stream()
				.filter(score -> score.getBattingTeamName().equals(teamName))
				.mapToInt(score -> score.getRuns() + score.getExtraRuns())
				.sum();
	}
	
	public int getTeamRunsGiven(String teamName) {
		return scores.stream()
				.filter(score -> score.getBowlingTeamName().equals(teamName))
				.mapToInt(score -> score.getRuns() + score.getExtraRuns())
				.sum();
	}
	
	public BigDecimal calculateBonusBowlingPoints(Player player) {
		
		BigDecimal teamEconomyRate = getTeamEconomyRate(getTeam(player));
		BigDecimal playerEconomyRate = getPlayerEconomyRate(player);
		
		BigDecimal diff = teamEconomyRate.subtract(playerEconomyRate);
		
		if(diff.signum() == 1) {
			if(isGreater(diff, teamEconomyRate.multiply(BOWLING_BONUS_FACTOR))) {
				return calculateBaseBowlingPoints(player).multiply(BOWLING_BONUS_FACTOR);
			}
		}
		
		if(diff.signum() == -1) {
			diff = diff.negate();
			if(isGreater(diff, teamEconomyRate.multiply(BOWLING_BONUS_FACTOR))) {
				return calculateBaseBowlingPoints(player).multiply(BOWLING_BONUS_FACTOR).negate();
			}
		}
		
		return BigDecimal.ZERO;
	}
	
	public BigDecimal getPlayerEconomyRate(Player player) {
		
		int runsConceded = getRunsConceded(player);
		
		long ballsBowled = getDeliveries(player);
		
		if(ballsBowled == 0) {
			return BigDecimal.ZERO;
		}
		
		return new BigDecimal(runsConceded).divide(new BigDecimal(ballsBowled), 2, RoundingMode.HALF_UP);
	}
	
	public int getRunsConceded(Player player) {
		return scores.stream()	
				.filter(score -> player.equals(score.getBowler()))
				.mapToInt(score -> score.getRuns() + score.getExtraRuns())
				.sum();
	}
	
	public int getDeliveries(Player player) {
		return (int) scores.stream()	
				.filter(Score::isNonExtraDelivery)
				.filter(score -> player.equals(score.getBowler()))
				.count();
	}
}
