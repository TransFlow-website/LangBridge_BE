# Document Lock 정보

## 데이터베이스 테이블

락 정보는 `document_lock` 테이블에 저장됩니다.

### 테이블 구조

```sql
CREATE TABLE document_lock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL UNIQUE,
    locked_by BIGINT NOT NULL,
    locked_at DATETIME NOT NULL,
    handover_memo TEXT,
    completed_paragraphs TEXT,
    FOREIGN KEY (document_id) REFERENCES document(id),
    FOREIGN KEY (locked_by) REFERENCES user(id)
);
```

### 주요 필드

- `id`: 락 ID (자동 증가)
- `document_id`: 문서 ID (UNIQUE 제약조건 - 한 문서당 하나의 락만 가능)
- `locked_by`: 락을 보유한 사용자 ID
- `locked_at`: 락 획득 시각
- `handover_memo`: 인계 메모 (인계 요청 시 저장)
- `completed_paragraphs`: 완료된 문단 ID 배열 (JSON 형식)

## 락 작동 방식

### 1. 락 획득 (`acquireLock`)

1. 문서 존재 확인
2. 사용자 확인 (없으면 기본 사용자 사용)
3. 기존 락 확인 (비관적 락 사용)
4. 같은 사용자가 이미 락을 보유하면 기존 락 반환
5. 다른 사용자가 락을 보유하면 409 CONFLICT 에러
6. 락이 없으면 새 락 생성 및 DB 저장
7. 문서 상태를 `IN_TRANSLATION`으로 변경

### 2. 락 상태 조회 (`getLockStatus`)

1. `document_lock` 테이블에서 조회 (JOIN FETCH로 LAZY 로딩 문제 해결)
2. 락이 없으면 `locked: false` 반환
3. 락이 있으면 `locked: true` 및 사용자 정보 반환
4. 현재 사용자가 락을 보유했는지 확인하여 `canEdit` 설정

### 3. 락 해제 (`releaseLock`)

1. 락 존재 확인
2. 권한 확인 (락을 보유한 사용자만 해제 가능)
3. `document_lock` 테이블에서 삭제

## 락 확인 방법

### DB에서 직접 확인

```sql
-- 모든 락 조회
SELECT * FROM document_lock;

-- 특정 문서의 락 조회
SELECT 
    dl.id as lock_id,
    dl.document_id,
    d.title as document_title,
    dl.locked_by,
    u.name as locked_by_name,
    u.email as locked_by_email,
    dl.locked_at,
    dl.handover_memo,
    dl.completed_paragraphs
FROM document_lock dl
JOIN document d ON dl.document_id = d.id
JOIN user u ON dl.locked_by = u.id
WHERE dl.document_id = ?;
```

### 백엔드 로그 확인

락 관련 로그는 다음과 같은 형식으로 출력됩니다:

- `✅ 문서 락 획득 및 DB 저장 완료`: 락이 성공적으로 저장됨
- `✅ 락 DB 저장 확인됨`: DB에 실제로 저장되었는지 확인
- `✅ 문서 상태 업데이트 완료`: 문서 상태가 `IN_TRANSLATION`으로 변경됨
- `🔍 락 상태 조회 시작`: 락 상태 조회 시작
- `✅ 락 발견`: 락이 존재함
- `ℹ️ 락이 없습니다`: 락이 없음

## 문제 해결

### 500 에러 발생 시

1. **LAZY 로딩 문제**: `findByDocumentIdWithUser` 메서드가 JOIN FETCH를 사용하여 해결
2. **트랜잭션 문제**: `@Transactional(readOnly = true)`로 트랜잭션 내에서 조회
3. **에러 처리**: 예외 발생 시 빈 Optional 반환하여 500 에러 방지

### 락이 저장되지 않는 경우

1. DB 연결 확인
2. 트랜잭션 커밋 확인
3. 백엔드 로그에서 "✅ 락 DB 저장 확인됨" 메시지 확인
4. DB에서 직접 `SELECT * FROM document_lock` 쿼리 실행

### "내가 작업 중인 문서"에 나타나지 않는 경우

1. 문서 상태가 `IN_TRANSLATION`인지 확인
2. 락이 DB에 저장되었는지 확인
3. 현재 사용자 ID와 락의 `locked_by`가 일치하는지 확인
4. 프론트엔드 콘솔 로그 확인

