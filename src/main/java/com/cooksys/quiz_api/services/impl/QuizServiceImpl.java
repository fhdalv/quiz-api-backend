package com.cooksys.quiz_api.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.cooksys.quiz_api.dtos.QuestionRequestDto;
import com.cooksys.quiz_api.dtos.QuestionResponseDto;
import com.cooksys.quiz_api.dtos.QuizRequestDto;
import com.cooksys.quiz_api.dtos.QuizResponseDto;
import com.cooksys.quiz_api.entities.Answer;
import com.cooksys.quiz_api.entities.Question;
import com.cooksys.quiz_api.entities.Quiz;
import com.cooksys.quiz_api.mappers.QuestionMapper;
import com.cooksys.quiz_api.mappers.QuizMapper;
import com.cooksys.quiz_api.repositories.AnswerRepository;
import com.cooksys.quiz_api.repositories.QuestionRepository;
import com.cooksys.quiz_api.repositories.QuizRepository;
import com.cooksys.quiz_api.services.QuizService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

	private final QuizRepository quizRepository;
	private final QuizMapper quizMapper;

	private final QuestionRepository questionRepository;
	private final QuestionMapper questionMapper;
	
	private final AnswerRepository answerRepository;

	@Override
	public List<QuizResponseDto> getAllQuizzes() {
		//return quizMapper.entitiesToDtos(quizRepository.findAll());
		return quizMapper.entitiesToDtos(quizRepository.findAllIsDeletedFalse());
	}

	@Override
	public QuizResponseDto createQuiz(QuizRequestDto quizRequestDto) {
		if(quizRequestDto == null || quizRequestDto.getName() == null) {
			return null;
		}
		Quiz quizToSave = quizMapper.requestDtoToEntity(quizRequestDto);
		Quiz quizCreated = quizRepository.saveAndFlush(quizToSave);
		for (Question question : quizCreated.getQuestions()) {
			question.setQuiz(quizCreated);
			questionRepository.saveAndFlush(question);
			for (Answer answer: question.getAnswers()) {
				answer.setQuestion(question);
			}
			answerRepository.saveAllAndFlush(question.getAnswers());
		}
		return quizMapper.entityToDto(quizCreated);
	}

	@Override
	public QuizResponseDto deleteQuiz(Long id) {
		Optional<Quiz> quizPotential = quizRepository.findById(id);
		if(quizPotential.isEmpty()){
			return null;
		}
		Quiz quizToDelete = quizPotential.get();
		if(quizToDelete.isDeleted()) {
			return null;
		}
//		for( Question question: quizToDelete.getQuestions()) {
//			answerRepository.deleteAll(question.getAnswers());
//			for(Answer answer: question.getAnswers()) {
			//answer.setDeleted(true);			//FLAG
//			}
			//question.setDeleted(true);		//FLAG
//		}
//		questionRepository.deleteAll(quizToDelete.getQuestions());
		//quizToDelete.setDeleted(true);		//FLAG
		//quizRepository.saveAndFlush(quizToDelete);	//FLAG
		//we changed the entity OnetoMany relations to have cascade property so it automatically references foreign key to delete all of them.
		quizRepository.delete(quizToDelete);
		return quizMapper.entityToDto(quizToDelete);
	}

	@Override
	public QuizResponseDto renamedQuiz(Long id, String newName) {
		Optional<Quiz> quizToUpdate = quizRepository.findById(id);
		if(quizToUpdate.isEmpty()) {
			return null;
		}
		Quiz updatedQuiz = quizToUpdate.get();
		if(updatedQuiz.isDeleted()) {
			return null;
		}
		updatedQuiz.setName(newName);
		return quizMapper.entityToDto(quizRepository.saveAndFlush(updatedQuiz));
	}

	@Override
	public QuestionResponseDto randomQuiz(Long id) {
		Optional<Quiz> quizToFind = quizRepository.findById(id);
		if(quizToFind.isEmpty()) {
			return null;
		}
		Quiz quiz = quizToFind.get();
		if(quiz.isDeleted()) {
			return null;
		}
		ArrayList<Question> filteredQuestion = new ArrayList<>();
		for(Question question: quiz.getQuestions()) {
			if(!question.isDeleted()) {
				filteredQuestion.add(question);
			}
		}
		Random random = new Random();
		//int randomNumber = random.nextInt(quiz.getQuestions().size());
		int randomNumber = random.nextInt(filteredQuestion.size());
		
		//return questionMapper.entityToDto(quiz.getQuestions().get(randomNumber));
		return questionMapper.entityToDto(filteredQuestion.get(randomNumber));

	}

	@Override
	public QuizResponseDto modifiedQuiz(Long id, QuestionRequestDto questionRequestDto) {
		Optional<Quiz> quizToAdd = quizRepository.findById(id);
		if(quizToAdd.isEmpty()) {
			return null;
		}
		Quiz quiz = quizToAdd.get();
		Question newQuestion = questionMapper.questionDtoToEntity(questionRequestDto);
		if(newQuestion.getText() == null) {
			return null;
		}
		newQuestion.setQuiz(quiz);
		if(newQuestion.getAnswers() == null) {
			return null;
		}
		for(Answer answer: newQuestion.getAnswers()) {
			if(answer.getText() == null) {
				return null;
			}
			answer.setQuestion(newQuestion);
		}
		questionRepository.saveAndFlush(newQuestion);
		answerRepository.saveAllAndFlush(newQuestion.getAnswers());
		Question questionToSave = questionRepository.saveAndFlush(newQuestion);
		
		return quizMapper.entityToDto(quiz);
	}

	@Override
	public QuestionResponseDto deleteQuestion(Long id, Long question_id) {
		Optional<Quiz> quizToFind = quizRepository.findById(id);
		if(quizToFind.isEmpty()) {
			return null;
		}
		Quiz quiz = quizToFind.get();
		if(quiz.isDeleted()) {
			return null;
		}
		
		Optional<Question> questionToFind = questionRepository.findById(question_id);
		if(questionToFind.isEmpty()) {
			return null;
		}
		Question question = questionToFind.get();
		if(question.isDeleted()) {
			return null;
		}
		if(!question.getQuiz().equals(quiz)) {
			return null;
		}
		for(Answer answer: question.getAnswers()) {
			answer.setDeleted(true);
		}
		question.setDeleted(true);
		//answerRepository.deleteAll(question.getAnswers());
		questionRepository.saveAndFlush(question);
		
		//questionRepository.deleteById(question_id);
		return questionMapper.entityToDto(question);
	}

}