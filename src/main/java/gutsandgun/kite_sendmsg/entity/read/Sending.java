package gutsandgun.kite_sendmsg.entity.read;
import gutsandgun.kite_sendmsg.entity.BaseTimeEntity;
import gutsandgun.kite_sendmsg.type.SendingRuleType;
import gutsandgun.kite_sendmsg.type.SendingType;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;


@Entity
@Getter
@Setter
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE sending SET is_deleted=true WHERE id = ?")
@Table(name = "sending")
public class Sending extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * user id
     */
    @Column(name = "fk_user_id")
    @Comment("user id")
    private String userId;

    /**
     * 분배 규칙 타입
     */
    @Comment("분배 규칙 타입")
    @Enumerated(EnumType.STRING)
    private SendingRuleType sendingRuleType;

    /**
     * 발송 메시지 타입
     */
    @Comment("발송 메시지 타입")
    @Enumerated(EnumType.STRING)
    private SendingType sendingType;

    @Comment("대체발송 여부")
    private String replaceYn;

    /**
     * 전체 메시지 개수
     */
    @Comment("전체 메시지 개수")
    private Long totalMessage;

    /**
     * 입력 시간 unix time
     */
    @Comment("입력 시간")
    private Long inputTime;

    /**
     * 예약 시간 unix time
     */
    @Comment("예약 시간")
    private Long scheduleTime;
    /**
     * 메시지 제목
     */
    @Comment("제목")
    private String title;

    /**
     * 미디어 호스팅 주소
     */
    @Comment("미디어 호스팅 주소")
    private String mediaLink;

    /**
     * 메시지 내용
     */
    @Comment("메시지 내용")
    private String content;

    @Comment("발신자")
    private String sender;

    @ColumnDefault("false")
    private Boolean isDeleted = false;

    @Comment("생성자")
    @Column(name = "reg_id", nullable = false, length = 20)
    private String regId;

    @Comment("수정자")
    @Column(name = "mod_id", length = 20)
    private String modId;
}