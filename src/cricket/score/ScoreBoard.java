package cricket.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cricket.Player;

public class ScoreBoard {

	private static final BigDecimal NON_ASSIST_BOWLING_POINTS = new BigDecimal(25);
	private static final BigDecimal ASSIST_BOWLING_POINTS = new BigDecimal(12.5);
	
	private final List<Score> scores;

	public ScoreBoard(List<Score> scores) {
		this.scores = new ArrayList<>(scores);
	}
	
	public Set<Player> getManOfMatch() {
		
		Comparator<Player> pointsComparator = (p1, p2) -> calculateMatchPoints(p2).compareTo(calculateMatchPoints(p1));
		
		List<Player> rankings = scores.stream()
			.flatMap(this::playerStream)
			.distinct()
			.sorted(pointsComparator)
			.collect(Collectors.toList());
		
//		System.out.println(rankings);
		
		Player firstOne = rankings.get(0);
		
		Set<Player> result = rankings
			.stream()
			.filter(p -> pointsComparator.compare(firstOne, p) == 0)
			.collect(Collectors.toSet());
		
		return result;
	}
	
	private Stream<Player> playerStream(Score score) {
		return  score.getPlayers().stream();
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
	
	
	public BigDecimal calculateMatchPoints(Player player) {
		
		return calculateBasePoints(player)
				.add(calculateBonusPoints(player));
	}

	public int calculateBaseBattingPoints(Player player) {
		
		return scores.stream()
				.filter(score -> player.equals(new Player(score.getBatsman())))
				.mapToInt(Score::getRuns)
				.sum();
	}
	
	public BigDecimal calculateBaseBowlingPoints(Player player) {
		
		return scores.stream()
				.filter(score -> player.equals(new Player(score.getBowler())))
				.filter(Score::isDismissalDelivery)
				.map(score -> {
					
					if(score.getAssistingPlayer().isEmpty() || player.equals(new Player(score.getAssistingPlayer()))) {
						return NON_ASSIST_BOWLING_POINTS;
					} else {
						return ASSIST_BOWLING_POINTS;
					}
				})
				.reduce(new BigDecimal(0), (a,b) -> a.add(b));
	}

	public BigDecimal calculateBaseFieldingPoints(Player player) {
		return scores.stream()
			.filter(score -> !score.getAssistingPlayer().isEmpty())
			.filter(score -> player.equals(new Player(score.getAssistingPlayer())))
			.map(score ->{
				if(score.getAssistingPlayer().equals(score.getBowler())) {
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
		
		BigDecimal teamStrikeRate = getTeamStrikeRate();
		BigDecimal playerStrikeRate = getPlayerStrikeRate(player);
		
		BigDecimal diff = playerStrikeRate.subtract(teamStrikeRate);
		
		if(diff.signum() == 1) {
			if(isGreater(diff, teamStrikeRate.multiply(BigDecimal.valueOf(0.1)))) {
				return calculateBaseBowlingPoints(player).multiply(BigDecimal.valueOf(0.1));
			}
		}
		
		if(diff.signum() == -1) {
			diff = diff.negate();
			if(isGreater(diff, teamStrikeRate.multiply(BigDecimal.valueOf(0.1)))) {
				return calculateBaseBowlingPoints(player).multiply(BigDecimal.valueOf(0.1)).negate();
			}
		}
		
		return BigDecimal.ZERO;
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
				.filter(score -> player.equals(new Player(score.getBatsman())))				
				.count();
	}

	public int getPlayerRuns(Player player) {
		return scores.stream()	
				.filter(score -> player.equals(new Player(score.getBatsman())))
				.mapToInt(score -> score.getRuns())
				.sum();
	}

	public BigDecimal getTeamStrikeRate() {
		
		int totalRuns = getTeamRuns();
			
		long totalBalls = getTeamBalls();
		
		return new BigDecimal(totalRuns).divide(new BigDecimal(totalBalls), 2, RoundingMode.HALF_UP);
	}

	public long getTeamBalls() {
		return scores.stream()
				.filter(Score::isNonExtraDelivery)
				.count();
	}

	public int getTeamRuns() {
		return scores.stream()
				.mapToInt(score -> score.getRuns() + score.getExtraRuns())
				.sum();
	}
	
	public BigDecimal calculateBonusBowlingPoints(Player player) {
		
		BigDecimal teamEconomyRate = getTeamStrikeRate();
		BigDecimal playerEconomyRate = getPlayerEconomyRate(player);
		
		BigDecimal diff = teamEconomyRate.subtract(playerEconomyRate);
		
		if(diff.signum() == 1) {
			if(isGreater(diff, teamEconomyRate.multiply(BigDecimal.valueOf(0.1)))) {
				return calculateBaseBowlingPoints(player).multiply(BigDecimal.valueOf(0.1));
			}
		}
		
		if(diff.signum() == -1) {
			diff = diff.negate();
			if(isGreater(diff, teamEconomyRate.multiply(BigDecimal.valueOf(0.1)))) {
				return calculateBaseBowlingPoints(player).multiply(BigDecimal.valueOf(0.1)).negate();
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
				.filter(score -> player.equals(new Player(score.getBowler())))
				.mapToInt(score -> score.getRuns() + score.getExtraRuns())
				.sum();
	}
	
	public int getDeliveries(Player player) {
		return (int) scores.stream()	
				.filter(Score::isNonExtraDelivery)
				.filter(score -> player.equals(new Player(score.getBowler())))
				.count();
	}

}
