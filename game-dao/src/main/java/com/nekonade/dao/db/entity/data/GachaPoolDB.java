package com.nekonade.dao.db.entity.data;


import com.nekonade.common.draw.DrawProb;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document("GachaPoolDB")
public class GachaPoolDB {

    @Id
    private String gachaPoolId;

    private boolean isActive;

    private long starTime;

    private long endTime;

    private List<GachaPoolCharacter> characters;

    private int costDiamond = 1;

    @Getter
    @Setter
    public static class GachaPoolCharacter extends DrawProb {

        @Indexed(unique = true,sparse = true)
        private String characterId;

    }
}
