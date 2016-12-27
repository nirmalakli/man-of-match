package cricket.score;

import java.util.HashSet;
import java.util.Set;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import cricket.Player;

public class Score {

	private final int inningsNumber;
	private final int overNumber;
	private final int ballNumber;
	private final String battingTeamName;
	private final String batsman;
	private final String nonStriker;
	private final String bowler;
	private final int runs;
	private final int extraRuns;
	private final String kindOfWicket;// TODO
	private final String dismissedPlayer;
	private final String assistingPlayer;
	
	private final String extra;
	
	public Score(int inningsNumber, int overNumber, int ballNumber, String battingTeamName, String batsman,
			String nonStriker, String bowler, int runs, String extra, String kindOfWicket, String dismissedPlayer,
			String assistingPlayer) {
		this.inningsNumber = inningsNumber;
		this.overNumber = overNumber;
		this.ballNumber = ballNumber;
		this.battingTeamName = battingTeamName;
		this.batsman = batsman;
		this.nonStriker = nonStriker;
		this.bowler = bowler;
		this.runs = runs;		
		this.extra = extra;
		this.kindOfWicket = kindOfWicket;
		this.dismissedPlayer = dismissedPlayer;
		this.assistingPlayer = assistingPlayer;
		
		this.extraRuns = Integer.valueOf(extra.substring(0,1));
	}

	public static Score from(String line) {
		String[] tokens = line.split(",");
		
		/*int i = 0;
		for(String token : tokens) {
			System.out.println(i++  + " = " + token);
		}*/
		
		int inningsNumber = Integer.valueOf(tokens[0].trim());
		int overNumber = Integer.valueOf(tokens[1].trim().split("\\.")[0]);
		int ballNumber = Integer.valueOf(tokens[1].trim().split("\\.")[1]);
		String battingTeamName = tokens[2].trim();
		String batsman = tokens[3].trim();
		String nonStriker = tokens[4].trim();
		String bowler = tokens[5].trim();
		int runs = Integer.valueOf(tokens[6].trim());
		String trim = tokens[7].trim();		
		String kindOfWicket = (tokens.length < 9) ? "" : tokens[8].trim(); // TODO
		String dismissedPlayer = (tokens.length < 10) ? "" : tokens[9].trim();
		String assistingPlayer = (tokens.length < 11) ? "" : tokens[10].trim();
		
		return new Score(inningsNumber, overNumber, ballNumber, battingTeamName, batsman, nonStriker, bowler, runs, trim, kindOfWicket, dismissedPlayer, assistingPlayer);
	}

	public int getInningsNumber() {
		return inningsNumber;
	}

	public int getOverNumber() {
		return overNumber;
	}

	public int getBallNumber() {
		return ballNumber;
	}

	public String getBattingTeamName() {
		return battingTeamName;
	}

	public String getBatsman() {
		return batsman;
	}

	public String getNonStriker() {
		return nonStriker;
	}

	public String getBowler() {
		return bowler;
	}

	public int getRuns() {
		return runs;
	}

	public int getExtraRuns() {
		return extraRuns;
	}

	public String getKindOfWicket() {
		return kindOfWicket;
	}

	public String getDismissedPlayer() {
		return dismissedPlayer;
	}

	public String getAssistingPlayer() {
		return assistingPlayer;
	}

	public Set<Player> getPlayers() {
		Set<Player> players = new HashSet<>();
		players.add(new Player(batsman));
		players.add(new Player(bowler));
		players.add(new Player(nonStriker));
		return players;
	}
	
	public boolean isDismissalDelivery() {
		return !kindOfWicket.isEmpty();
	}
	
	public boolean isExtraDelivery() {
		return extra.length() > 0;
	}
	
	public boolean isNonExtraDelivery() {
		return extra.length() == 1;
	}


	@Override
	public String toString() {
		return String.format(
				"Score [overNumber=%s, ballNumber=%s, batsman=%s, bowler=%s, runs=%s, extras=%s, kindOfWicket=%s, dismissedPlayer=%s, assistingPlayer=%s]",
				overNumber, ballNumber, batsman, bowler, runs, extra, kindOfWicket, dismissedPlayer, assistingPlayer);
	}
	
	

}
