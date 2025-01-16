package si.um.feri.cestar.Utils;

import com.badlogic.gdx.math.MathUtils;
import java.util.List;
import si.um.feri.cestar.screens.QuestionScreen;

public class GameManager {

    private static GameManager instance;

    private int score;
    private Question currentQuestion;
    private QuestionUtils questionUtils;
    private String currentUser;

    private GameManager() {
        this.score = 0;
        this.questionUtils = new QuestionUtils();
    }


    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }


    public Question getRandomQuestion(QuestionScreen.Difficulty difficulty) {
        List<Question> questions = questionUtils.getQuestionMap().get(difficulty);
        if (questions != null && !questions.isEmpty()) {
            currentQuestion = questions.get(MathUtils.random(questions.size() - 1));
            return currentQuestion;
        }
        return null;
    }


    public boolean isAnswerCorrect(int selectedAnswerIndex) {
        if (currentQuestion != null) {
            return selectedAnswerIndex == currentQuestion.getCorrectAnswerIndex();
        }
        return false;
    }


    public void updateScore(QuestionScreen.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                score += 10;
                break;
            case MEDIUM:
                score += 20;
                break;
            case HARD:
                score += 30;
                break;
        }
    }

    public int getScore() {
        return score;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setScore(int score) {
        this.score = score;
    }


    public void resetScore() {
        this.score = 0;
    }

}
