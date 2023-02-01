package gutsandgun.kite_sendmsg.entity.read;

import com.fasterxml.jackson.annotation.JsonFormat;
import gutsandgun.kite_sendmsg.type.SendingRuleType;
import gutsandgun.kite_sendmsg.type.SendingType;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Where(clause = "is_deleted = false")
@SQLDelete(sql= "UPDATE sending SET is_deleted=true WHERE id = ?")
@Table(name="sending")
@DynamicInsert
@ToString
public class Sending {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fk_user_id")
    private String userId;

    @Comment("분배규칙타입")
    @Enumerated(EnumType.STRING)
    private SendingRuleType sendingRuleType;

    @Comment("발송타입")
    @Enumerated(EnumType.STRING)
    private SendingType sendingType;

    @Comment("대체발송 여부")
    private String replaceYn;

    @Comment("메세지 갯수")
    private Long totalSending;

    @Comment("입력시각")
    private Long inputTime;

    @Comment("예약시각")
    private Long scheduleTime;

    @Comment("제목")
    private String title;

    @Comment("미디어링크")
    private String mediaLink;

    @Comment("문자 내용")
    private String content;

    @ColumnDefault("false")
    private Boolean isDeleted = false;

    @Comment("생성일자")
    @CreatedDate
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Comment("수정일자")
    @LastModifiedDate
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Column(name = "mod_dt")
    private LocalDateTime modDt;

    @Comment("생성자")
    @Column(name = "reg_id", nullable = false, length = 20)
    private String regId;

    @Comment("수정자")
    @Column(name = "mod_id", length = 20)
    private String ModId;

}