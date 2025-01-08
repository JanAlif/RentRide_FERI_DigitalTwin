package si.um.feri.cestar.Utils;

import java.util.List;

public class Question {

    private String questionText;
    private List<String> answers; // List of possible answers
    private int correctAnswerIndex; // Index of the correct answer in the list

    public Question(String questionText, List<String> answers, int correctAnswerIndex) {
        this.questionText = questionText;
        this.answers = answers;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public String getCorrectAnswer() {
        return answers.get(correctAnswerIndex);
    }


}
