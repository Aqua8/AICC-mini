package com.aicc.backend.domain;

import java.util.List;

/** AI·LLM 상담 서비스의 기본 카테고리. 카테고리 테이블이 비어 있을 때 채워 넣는다. */
public final class DefaultCategories {

    public record Item(String name, String description) {
    }

    public static final List<Item> ITEMS = List.of(
            new Item("LLM 기초", "LLM 동작 원리, 토큰, 컨텍스트 윈도우, 환각 같은 기본 개념"),
            new Item("프롬프트 작성", "프롬프트 설계, 시스템 프롬프트, 출력 형식 지정, 퓨샷"),
            new Item("API·모델 활용", "LLM API 연동, 모델 선택, 파라미터, 스트리밍, 오류 대응"),
            new Item("RAG·검색", "임베딩, 벡터 DB, 문서 검색 기반 답변 구성"),
            new Item("파인튜닝·학습", "파인튜닝, 데이터셋 준비, 평가 방법"),
            new Item("에이전트·도구 사용", "함수 호출, MCP, 에이전트 설계, 멀티 에이전트"),
            new Item("비용·성능", "토큰 비용, 캐싱, 지연 시간, 모델 경량화"),
            new Item("AI 안전·윤리", "개인정보, 프롬프트 인젝션, 편향, 저작권, 책임 있는 사용"),
            new Item("기타", "위 분류에 맞지 않는 AI·LLM 관련 문의")
    );

    private DefaultCategories() {
    }

    public static List<String> names() {
        return ITEMS.stream().map(Item::name).toList();
    }
}
