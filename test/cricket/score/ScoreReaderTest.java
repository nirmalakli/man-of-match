package cricket.score;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import cricket.Player;

public class ScoreReaderTest {
	
	@Test
	public void canReadScores() throws Exception {
		ScoreReader scoreReader = new ScoreReader("scores.txt");
		List<Score> scores = scoreReader.getScores();
		assertNotNull(scores);
		assertEquals(13, scores.size());
		
		Score score = scores.get(0);
		assertNotNull(score);
		assertEquals(1, score.getInningsNumber());
		assertEquals(0, score.getOverNumber());
		assertEquals(3, score.getBallNumber());
		assertEquals("Kolkata Knight Riders", score.getBattingTeamName());
		assertEquals("BB McCullum", score.getBatsman());
		assertEquals("SC Ganguly", score.getNonStriker());
		assertEquals("P Kumar", score.getBowler());
		assertEquals(0, score.getRuns());
		assertEquals(1, score.getExtraRuns());
		assertEquals("", score.getKindOfWicket());
		assertEquals("", score.getDismissedPlayer());
		assertEquals("", score.getAssistingPlayer());
	}
	
	
	@Test
	public void pickPlayersInAScore() throws Exception {
		Score score = new ScoreReader("scores.txt").getScores().get(0);
		Set<Player> players = score.getPlayers();
		assertEquals(3, players.size());
	}
	

}
