package com.nekonade.dao.db.entity;


import com.nekonade.common.model.LogRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@Document("LogGameLogicRequest")
public class LogGameLogicRequest extends LogRequest {

}
