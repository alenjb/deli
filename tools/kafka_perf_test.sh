#!/bin/bash

# 로그 저장 경로
LOG_FILE="console_kafka_perf.log"
ANALYSIS_FILE="kafka_analysis.txt"

# 현재 시간 출력용
timestamp=$(date +"%Y-%m-%d %H:%M:%S")
echo "Kafka 자동 성능 테스트 시작 (${timestamp})"

# 기존 로그 초기화
rm -f "$LOG_FILE" "$ANALYSIS_FILE"

# 1~5번 order 생성 및 Kafka 메시지 전송 (각 10회)
for ORDER_ID in {1..5}
do
  for i in {1..10}
  do
    # 현재 시간 ISO-8601 형식으로
    deliveredAt=$(date +"%Y-%m-%dT%H:%M:%S")

    # Kafka 메시지 전송
    response=$(curl -s -X POST "http://localhost:8080/orders/${ORDER_ID}/confirm?deliveredAt=${deliveredAt}")

    echo "[$(date +%H:%M:%S)] 요청 → orderId=${ORDER_ID}, deliveredAt=${deliveredAt}" >> "$LOG_FILE"
    echo "응답: $response" >> "$LOG_FILE"

    # 대기
    sleep 0.2
  done
done

echo ""
echo "메시지 전송 완료. 로그 분석 중..."

echo "📥 콘솔 로그에서 복사한 처리 시간 로그를 붙여넣으세요 (끝내려면 Ctrl+D):"
cat >> "$LOG_FILE"

# 처리 시간만 추출 후 분석
grep "Kafka 이벤트 전체 처리 시간" "$LOG_FILE" | awk '{
  match($0, /([0-9]+)ms/, arr)
  time = arr[1]
  sum += time
  count += 1
  if (time > max) max = time
  if (min == 0 || time < min) min = time
} END {
  if (count > 0) {
    print "Kafka 처리 시간 통계" > "'$ANALYSIS_FILE'"
    print "총 요청 수 :", count >> "'$ANALYSIS_FILE'"
    print "평균       :", sum / count "ms" >> "'$ANALYSIS_FILE'"
    print "최소       :", min "ms" >> "'$ANALYSIS_FILE'"
    print "최대       :", max "ms" >> "'$ANALYSIS_FILE'"
  } else {
    print "Kafka 처리 시간 로그가 없습니다." > "'$ANALYSIS_FILE'"
  }
}'

echo ""
cat "$ANALYSIS_FILE"
