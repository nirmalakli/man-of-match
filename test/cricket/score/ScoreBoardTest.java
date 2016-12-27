package cricket.score;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import cricket.Player;
import cricket.score.Score;
import cricket.score.ScoreBoard;
import cricket.score.ScoreReader;

public class ScoreBoardTest {
	
	private ScoreBoard match;
	private Player ganguly;
	private Player mcCullum;
	private Player pKumar;
	private Player zKhan;
	private Player kohli;
	private Player dravid;

	@Before
	public void setup() {
		List<Score> scores = new ScoreReader("scores.txt").getScores();
		match = new ScoreBoard(scores);
		ganguly = new Player("SC Ganguly");
		mcCullum = new Player("BB McCullum");
		zKhan = new Player("Z Khan");
		pKumar = new Player("P Kumar");
		kohli = new Player("Virat Kohli");
		dravid = new Player("Rahul Dravid"); 
	}

	@Test
	public void battingBasePointsAreCalculatedCorrectly() throws Exception {
		
		int battingPoints = match.calculateBaseBattingPoints(ganguly);
		assertEquals(0, battingPoints);
		
		battingPoints = match.calculateBaseBattingPoints(mcCullum);
		assertEquals(18, battingPoints);
		
		battingPoints = match.calculateBaseBattingPoints(pKumar);
		assertEquals(0, battingPoints);
		
		battingPoints = match.calculateBaseBattingPoints(zKhan);
		assertEquals(0, battingPoints);
	}
	
	@Test
	public void bowlingBasePointsAreCalculatedCorrectly() throws Exception {
		
		BigDecimal bowlingPoints = match.calculateBaseBowlingPoints(ganguly);
		assertEquals("0", bowlingPoints.toPlainString());
		
		bowlingPoints = match.calculateBaseBowlingPoints(mcCullum);
		assertEquals("0", bowlingPoints.toPlainString());
		
		bowlingPoints = match.calculateBaseBowlingPoints(pKumar);
		assertEquals("0", bowlingPoints.toPlainString());
		
		bowlingPoints = match.calculateBaseBowlingPoints(zKhan);
		assertEquals("37.5", bowlingPoints.toPlainString());
	}
	
	@Test
	public void fieldingBasePointsAreCalculatedCorrectly() throws Exception {
		BigDecimal fieldingPoints = match.calculateBaseFieldingPoints(ganguly);
		assertEquals("0", fieldingPoints.toPlainString());
		
		fieldingPoints = match.calculateBaseFieldingPoints(mcCullum);
		assertEquals("0", fieldingPoints.toPlainString());
		
		fieldingPoints = match.calculateBaseFieldingPoints(pKumar);
		assertEquals("0", fieldingPoints.toPlainString());
		
		fieldingPoints = match.calculateBaseFieldingPoints(zKhan);
		assertEquals("0", fieldingPoints.toPlainString());
		
		fieldingPoints = match.calculateBaseFieldingPoints(kohli);
		assertEquals("12.5", fieldingPoints.toPlainString());
	}
	
	@Test
	public void teamRunsIsCorrect() {
		int teamRuns = match.getTeamRuns();
		assertEquals(21, teamRuns);
	}
	
	@Test
	public void teamBallsPlayedIsCorrect() {
		long teamBalls = match.getTeamBalls();
		assertEquals(11, teamBalls);
	}
	
	@Test
	public void teamStrikeRateIsCorrect() {
		BigDecimal teamStrikeRate = match.getTeamStrikeRate();
		assertEquals("1.91", teamStrikeRate.toPlainString());
	}
	
	@Test
	public void playerRunsIsCorrect() {
		int runs = match.getPlayerRuns(ganguly);
		assertEquals(0, runs);
		
		runs = match.getPlayerRuns(mcCullum);
		assertEquals(18, runs);
		
		runs = match.getPlayerRuns(dravid);
		assertEquals(1, runs);
	}
	
	@Test
	public void playerBallsFacedIsCorrect() {
		long balls = match.getBallsFacedByPlayer(ganguly);
		assertEquals(0, balls);
		
		balls = match.getBallsFacedByPlayer(mcCullum);
		assertEquals(10, balls);
		
		balls = match.getBallsFacedByPlayer(dravid);
		assertEquals(1, balls);
	}
	
	
	@Test
	public void playerStrikeRateIsCorrect() {
		BigDecimal playerStrikeRate = match.getPlayerStrikeRate(ganguly);
		assertEquals("0", playerStrikeRate.toPlainString());
		
		playerStrikeRate = match.getPlayerStrikeRate(mcCullum);
		assertEquals("1.80", playerStrikeRate.toPlainString());
		
		playerStrikeRate = match.getPlayerStrikeRate(dravid);
		assertEquals("1.00", playerStrikeRate.toPlainString());
	}
	
	@Test
	public void battingBonusPointsAreCalculatedCorrectly() throws Exception {
		BigDecimal battingPoints = match.calculateBonusBattingPoints(ganguly);
		assertEquals("0", battingPoints.toPlainString());
		
		battingPoints = match.calculateBonusBattingPoints(mcCullum);
		assertEquals("0", battingPoints.toPlainString());
	}
	
	@Test
	public void runsConcededIsCorrect() {
		int runs = match.getRunsConceded(ganguly);
		assertEquals(0, runs);
		
		runs = match.getRunsConceded(zKhan);
		assertEquals(19, runs);
		
		runs = match.getRunsConceded(pKumar);
		assertEquals(2, runs);
	}
	
	@Test
	public void deliveriesBowledIsCorrect() {
		int balls = match.getDeliveries(ganguly);
		assertEquals(0, balls);
		
		balls = match.getDeliveries(zKhan);
		assertEquals(8, balls);
		
		balls = match.getDeliveries(pKumar);
		assertEquals(3, balls);
	}
	
	@Test
	public void playerEconomyRateIsCorrect() {
		BigDecimal playerEconomyRate = match.getPlayerEconomyRate(ganguly);
		assertEquals("0", playerEconomyRate.toPlainString());
		
		playerEconomyRate = match.getPlayerEconomyRate(zKhan);
		assertEquals("2.38", playerEconomyRate.toPlainString());
		
		playerEconomyRate = match.getPlayerEconomyRate(pKumar);
		assertEquals("0.67", playerEconomyRate.toPlainString());
	}
	
	@Test
	public void bowlingBonusPointsAreCalculatedCorrectly() {
		
		BigDecimal bowlingPoints = match.calculateBonusBowlingPoints(ganguly);
		assertEquals("0.0", bowlingPoints.toPlainString());
		
		bowlingPoints = match.calculateBonusBowlingPoints(pKumar);
		assertEquals("0.0", bowlingPoints.toPlainString());
		
		bowlingPoints = match.calculateBonusBowlingPoints(zKhan);
		assertEquals("-3.75", bowlingPoints.toPlainString());
	}
	
	@Test
	public void totalBonusPoints() throws Exception {
		BigDecimal bonusPoints = match.calculateBonusPoints(zKhan);
		assertEquals("-3.75", bonusPoints.toPlainString());
	}
	
	@Test
	public void totalMatchPointsAreCorrect() {

		BigDecimal matchPoints = match.calculateMatchPoints(ganguly);
		assertEquals("0.0", matchPoints.toPlainString());
		
		matchPoints = match.calculateMatchPoints(mcCullum);
		assertEquals("18.0", matchPoints.toPlainString());
		
		matchPoints = match.calculateMatchPoints(pKumar);
		assertEquals("0.0", matchPoints.toPlainString());
		
		matchPoints = match.calculateMatchPoints(zKhan);
		assertEquals("33.75", matchPoints.toPlainString());
		
		matchPoints = match.calculateMatchPoints(kohli);
		assertEquals("12.5", matchPoints.toPlainString());
		
		matchPoints = match.calculateMatchPoints(dravid);
		assertEquals("1.0", matchPoints.toPlainString());
	}
}
