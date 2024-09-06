package br.com.alura.screenmatch.service;

public interface IConvertsData {
    <T> T getData(String json, Class<T> category);
}
