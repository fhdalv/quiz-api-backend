package com.cooksys.quiz_api.dtos;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AnswerRequestDto {
	
	private String text;
	
	private boolean correct;
	
}
