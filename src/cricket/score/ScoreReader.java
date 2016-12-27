package cricket.score;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreReader {

	private final List<Score> scores;

	public ScoreReader(String scoreFileName) {
		this.scores = new ArrayList<>();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(scoreFileName);
		if(is == null) {
			throw new IllegalArgumentException("Score file name: " + scoreFileName + " does not exist");
		}
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line = null;
		
		try {
			while ( (line = rd.readLine()) != null) {
				if(!line.trim().isEmpty()) {
					scores.add(Score.from(line.trim()));
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Could not read score file: " + scoreFileName);
		}
	}

	public List<Score> getScores() {
		return Collections.unmodifiableList(scores);
		
	}

}
