package com.example.webclientparent.model;

import lombok.Data;
import model.Token;

import java.util.List;

@Data
public class TokenWrapper {

    private List<Token> token;
}
