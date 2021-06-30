package com.nekonade.neko.service.test;


import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.dto.ItemDTO;
import com.nekonade.common.utils.FunctionMapper;
import com.nekonade.dao.daos.db.*;
import com.nekonade.dao.db.entity.MailBox;
import com.nekonade.dao.db.entity.Player;
import com.nekonade.dao.db.entity.data.*;
import com.nekonade.dao.db.entity.data.rbeffect.RaidBattleEffectDB;
import com.nekonade.dao.db.entity.data.rbeffect.RaidBattleEffectGroupDB;
import com.nekonade.dao.db.entity.data.task.ConsumeGoldTask;
import com.nekonade.dao.db.entity.data.task.DayFirstLoginTask;
import com.nekonade.dao.db.entity.data.task.SpecificStagePassTimesTask;
import com.nekonade.dao.db.entity.data.task.TaskDB;
import com.nekonade.dao.db.repository.*;
import com.nekonade.neko.logic.task.TaskEnumCollections;
import com.nekonade.network.message.event.function.EnterGameEvent;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.ScheduledFuture;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @ClassName: TestDataInitService
 * @Author: Lily
 * @Description: 用于所有测试DB数据的写入
 * @Date: 2021/6/29
 * @Version: 1.0
 */
@Service
@EnableScheduling
public class TestDataInitService {

    @Autowired
    private ItemDBRepository itemsDbRepository;

    @Autowired
    private GlobalConfigDao globalConfigDao;

    @Autowired
    private ItemDBDao itemsDbDao;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MailBoxRepository mailBoxRepository;

    @Autowired
    private RaidBattleDbRepository raidBattleDbRepository;

    @Autowired
    private RaidBattleDbDao raidBattleDbDao;
    
    @Autowired
    private RewardDbRepository rewardsDbRepository;

    @Autowired
    private RewardDBDao rewardsDbDao;

    @Autowired
    private RaidBattleEffectDBDao raidBattleEffectsDbDao;

    @Autowired
    private RaidBattleEffectsDbRepository raidBattleEffectsDbRepository;

    @Autowired
    private RaidBattleEffectGroupDBDao raidBattleEffectGroupsDbDao;

    @Autowired
    private RaidBattleEffectGroupsDbRepository raidBattleEffectGroupsDbRepository;

    @Autowired
    private SkillDBDao cardsDbDao;

    @Autowired
    private SkillDBRepository cardsDbRepository;

    @Autowired
    private CharacterDBDao charactersDbDao;

    @Autowired
    private CharacterDBRepository charactersDbRepository;

    @Autowired
    private EnemyDBDao enemyDBDao;

    @Autowired
    private EnemyDBRepository enemyDBRepository;

    @Autowired
    private GachaPoolDBDao gachaPoolsDbDao;

    @Autowired
    private GachaPoolDBRepository gachaPoolsDbRepository;

    @Autowired
    private TaskDBDao tasksDbDao;

    @Autowired
    private TaskDBRepository tasksDbRepository;

    @DataProvider(name = "ItemDbTestData")
    public static Object[][] ItemDbTestData() {
        ItemDB itemsDB1 = new ItemDB();
        itemsDB1.setItemId("1");
        itemsDB1.setName("Stamina回复药");
        itemsDB1.setType(2);
        ItemDB itemsDB2 = new ItemDB();
        itemsDB2.setItemId("2");
        itemsDB2.setName("BP回复药");
        itemsDB2.setCategory(1);
        itemsDB2.setType(2);
        ItemDB itemsDB3 = new ItemDB();
        itemsDB3.setItemId("3");
        itemsDB3.setName("奖励宝箱A");
        itemsDB3.setCategory(3);
        itemsDB3.setType(5);
        ItemDB itemsDB4 = new ItemDB();
        itemsDB4.setItemId("4");
        itemsDB4.setName("普通素材A");
        itemsDB4.setCategory(2);
        itemsDB4.setType(1);
        ItemDB itemsDB5 = new ItemDB();
        itemsDB5.setItemId("5");
        itemsDB5.setName("珍贵素材A");
        itemsDB5.setCategory(2);
        itemsDB5.setType(2);

        ItemDB itemsDB6 = new ItemDB();
        itemsDB6.setItemId("6");
        itemsDB6.setName("碎片");
        itemsDB6.setCategory(2);
        itemsDB6.setType(2);

        ItemDB itemsDB7 = new ItemDB();
        itemsDB7.setItemId("1000");
        itemsDB7.setName("钻石");
        itemsDB7.setCategory(5);
        itemsDB7.setType(1);

        return new Object[][]{
                {itemsDB1}, {itemsDB2}, {itemsDB3},
                {itemsDB4}, {itemsDB5}, {itemsDB6},
                {itemsDB7}
        };
    }

    @EventListener
    public void loginAddExp(EnterGameEvent event) {
        event.getPlayerManager().getExperienceManager().addExperience(1000);
    }

    @EventListener
    public void loginAddDiamond(EnterGameEvent event) {
        event.getPlayerManager().getDiamondManager().addDiamond(1000);
    }

    /*@EventListener
    public void addItem(EnterGameEvent event) {
        InventoryManager inventoryManager = event.getPlayerManager().getInventoryManager();
        List<ItemsDB> itemDbData = getItemDbData();
        itemDbData.forEach(item->{
            Random random = new Random();
            inventoryManager.produceItem(item.getItemId(), random.nextInt(20));
        });
    }*/

    private void InitItemsDB() {
        Object[][] objects = ItemDbTestData();
        List<ItemDB> itemsDBS = new ArrayList<>();
        for (Object[] object : objects) {
            for (Object obj : object) {
                ItemDB item = (ItemDB) obj;
                itemsDBS.add(item);
            }
        }
        itemsDBS.forEach(item -> {
            itemsDbRepository.deleteByItemId(item.getItemId());
            itemsDbDao.saveOrUpdateMap(item, item.getItemId());
        });
    }

    private List<ItemDB> getItemDbData() {
        return itemsDbRepository.findAll();
    }

    private ScheduledFuture<?> scheduledFuture;
    private final DefaultEventExecutor eventExecutors = new DefaultEventExecutor();

    @PostConstruct
    private void buildProto() {
        /*RaidBattleDamageDTO.Builder builder = RaidBattleDamageDTO.newBuilder();
        RaidBattleDamageDTO source = new RaidBattleDamageDTO();
        ProtoBufUtils.transformProtoReturnBuilder(builder,source);
        RaidBattleDamageDTO build = builder.build()*/
        System.out.println("Building...?");
    }

    @PostConstruct
    private void init() {

        scheduledFuture = eventExecutors.scheduleWithFixedDelay(() -> {
            try {
                InitItemsDB();
                InitEnemiesDB();
                InitRewardsDB();
                InitRaidBattleDB();
                SendMail();
                InitRaidBattleEffectGroupsDB();
                InitRaidBattleEffectsDB();
                InitCharactersDb();
                InitGachaPoolsDb();
                InitTasksDb();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);

        //mailBoxRepository.saveAll(mailBoxes);
    }

    private void SendMail() {
        List<MailBox> mailBoxes = new ArrayList<>();
        int times = RandomUtils.nextInt(1,4);
        for (int i = 0; i < times; i++) {
            List<MailBox> list = MailBoxTestData();
            if(list != null){
                mailBoxes.addAll(list);
            }
        }
        mailBoxes.forEach(each -> mailBoxRepository.save(each));
    }

    @Test(dataProvider = "ItemDbTestData")
    private void LoadItemDb(ItemDB itemsDB) {
//        itemDbService.addItemDb(itemsDB);
//        ItemsDB op = itemDbService.findByItemId(itemsDB.getItemId());
//        Assert.assertNotNull(op);
    }

    private List<MailBox> MailBoxTestData() {
        List<Player> all = playerRepository.findAll();
        if(all.size() == 0) return null;
        Player player = all.get(0);
        long senderId = player.getPlayerId();
        String senderName = player.getNickName();
        List<ItemDB> items = itemsDbRepository.findAll();
        long now = System.currentTimeMillis();
        return all.stream().map(Player::getPlayerId).map(id -> {
            MailBox mailBox = new MailBox();
            mailBox.setSenderId(senderId);
            mailBox.setSenderName(senderName);
            mailBox.setTitle(DigestUtils.md5Hex(id + senderName + player + Math.random()));
            mailBox.setContent("Send To PlayerId:" + id);
            mailBox.setTimestamp(now);
            mailBox.setExpired(now + Duration.ofDays(30).toMillis());
            mailBox.setReceiverId(id);
            Function<ItemDB, ItemDTO> mapper = FunctionMapper.Mapper(ItemDB.class, ItemDTO.class);
            Collections.shuffle(items);
            List<ItemDTO> list = items.stream().map(mapper).peek(each -> {
                Random random = new Random();
                each.setAmount(random.nextInt(10) + 1);
            }).collect(Collectors.toList());
            mailBox.setGifts(list);
            return mailBox;
        }).collect(Collectors.toList());
    }


    private String createStageRedisKey(String[] list) {
        return "STAGE_" + String.join("_", Arrays.asList(list));
    }

    private String createStageRedisKey(RaidBattleDB raidBattleDB){
        return MessageFormat.format("STAGE_{0}_{1}_{2}_{3}_{4}",raidBattleDB.getArea(),raidBattleDB.getEpisode(),raidBattleDB.getChapter(),raidBattleDB.getStage(),raidBattleDB.getDifficulty());
    }

    private void InitRaidBattleDB() {
        List<EnemyDB> enemiesDBS = getEnemiesDB();
        RaidBattleDB raidBattleDB = new RaidBattleDB();
        raidBattleDB.setArea(1);
        raidBattleDB.setEpisode(1);
        raidBattleDB.setChapter(1);
        raidBattleDB.setStage(1);
        raidBattleDB.setDifficulty(1);
        raidBattleDB.setEntryCostStaminaPoint(10);
        raidBattleDB.setMultiRaid(false);
        raidBattleDB.setLimitCounter(0L);
        raidBattleDB.setActive(true);
        String rkey = createStageRedisKey(raidBattleDB);
        raidBattleDB.setStageId(rkey);
        {

            List<String> enemyIds = new ArrayList<>();

            Optional<EnemyDB> test_monster_0001 = enemiesDBS.stream().filter(each -> each.getCharacterId().equals("TEST_MONSTER_0001")).findFirst();
            if (test_monster_0001.isPresent()) {
                enemyIds.add("TEST_MONSTER_0001");
                raidBattleDB.getEnemyList().add(test_monster_0001.get());
            }

            Optional<EnemyDB> test_monster_0002 = enemiesDBS.stream().filter(each -> each.getCharacterId().equals("TEST_MONSTER_0002")).findFirst();
            if (test_monster_0002.isPresent()) {
                enemyIds.add("TEST_MONSTER_0002");
                raidBattleDB.getEnemyList().add(test_monster_0002.get());
            }
            raidBattleDB.setEnemyIds(enemyIds);
        }

        RaidBattleDB raidBattleDB1 = new RaidBattleDB();
        raidBattleDB1.setArea(1);
        raidBattleDB1.setEpisode(1);
        raidBattleDB1.setChapter(1);
        raidBattleDB1.setStage(2);
        raidBattleDB1.setDifficulty(1);
        raidBattleDB1.setActive(true);
        Map<String, Integer> costItemMap1 = new HashMap<>();
        costItemMap1.put("4", 2);
        costItemMap1.put("5", 13);
        raidBattleDB1.setEntryCostItemMap(costItemMap1);
        raidBattleDB1.setEntryCostStaminaPoint(15);
        raidBattleDB1.setMultiRaid(true);
        raidBattleDB1.setLimitCounter(5L);
        raidBattleDB1.setLimitCounterRefreshType(EnumCollections.DataBaseMapper.EnumNumber.RaidBattle_Create_LimitCounterRefreshType_None.getValue());
        String rkey1 = createStageRedisKey(raidBattleDB1);
        raidBattleDB1.setStageId(rkey1);
        {
            List<String> enemyIds = new ArrayList<>();

            Optional<EnemyDB> test_monster_0003 = enemiesDBS.stream().filter(each -> each.getCharacterId().equals("TEST_MONSTER_0003")).findFirst();
            if (test_monster_0003.isPresent()) {
                enemyIds.add("TEST_MONSTER_0003");
                raidBattleDB1.getEnemyList().add(test_monster_0003.get());
            }

            Optional<EnemyDB> test_monster_0004 = enemiesDBS.stream().filter(each -> each.getCharacterId().equals("TEST_MONSTER_0004")).findFirst();
            if (test_monster_0004.isPresent()) {
                enemyIds.add("TEST_MONSTER_0004");
                raidBattleDB1.getEnemyList().add(test_monster_0004.get());
            }
            raidBattleDB1.setEnemyIds(enemyIds);
        }


        RaidBattleDB raidBattleDB2 = new RaidBattleDB();
        raidBattleDB2.setArea(1);
        raidBattleDB2.setEpisode(1);
        raidBattleDB2.setChapter(1);
        raidBattleDB2.setStage(3);
        raidBattleDB2.setDifficulty(1);
        raidBattleDB2.setActive(true);
        Map<String, Integer> costItemMap2 = new HashMap<>();
        costItemMap2.put("4", 10);
        costItemMap2.put("5", 5);
        raidBattleDB2.setEntryCostItemMap(costItemMap2);
        raidBattleDB2.setEntryCostStaminaPoint(15);
        raidBattleDB2.setMultiRaid(false);
        raidBattleDB2.setLimitCounter(10L);
        raidBattleDB2.setLimitCounterRefreshType(EnumCollections.DataBaseMapper.EnumNumber.Week_Tuesday.getValue());
        String rkey2 = createStageRedisKey(raidBattleDB2);
        raidBattleDB2.setStageId(rkey2);

        {
            List<String> enemyIds = new ArrayList<>();

            Optional<EnemyDB> test_monster_0005 = enemiesDBS.stream().filter(each -> each.getCharacterId().equals("TEST_MONSTER_0005")).findFirst();
            if (test_monster_0005.isPresent()) {
                enemyIds.add("TEST_MONSTER_0005");
                raidBattleDB2.getEnemyList().add(test_monster_0005.get());
            }
            raidBattleDB2.setEnemyIds(enemyIds);
        }

        RaidBattleDB raidBattleDB3 = new RaidBattleDB();
        raidBattleDB3.setArea(1);
        raidBattleDB3.setEpisode(1);
        raidBattleDB3.setChapter(1);
        raidBattleDB3.setStage(4);
        raidBattleDB3.setDifficulty(1);
        raidBattleDB3.setActive(true);
        raidBattleDB3.setEntryCostStaminaPoint(100);
        raidBattleDB3.setMultiRaid(false);
        raidBattleDB3.setLimitCounter(2L);
        raidBattleDB3.setLimitCounterRefreshType(EnumCollections.DataBaseMapper.EnumNumber.Week_Saturday.getValue());
        String rkey3 = createStageRedisKey(raidBattleDB3);
        raidBattleDB3.setStageId(rkey3);

        {
            List<String> enemyIds = new ArrayList<>();

            Optional<EnemyDB> test_monster_0005 = enemiesDBS.stream().filter(each -> each.getCharacterId().equals("TEST_MONSTER_0005")).findFirst();
            if (test_monster_0005.isPresent()) {
                enemyIds.add("TEST_MONSTER_0005");
                raidBattleDB3.getEnemyList().add(test_monster_0005.get());
            }
            raidBattleDB3.setEnemyIds(enemyIds);
        }


        RaidBattleDB raidBattleDB9999 = new RaidBattleDB();
        raidBattleDB9999.setArea(9);
        raidBattleDB9999.setEpisode(9);
        raidBattleDB9999.setChapter(9);
        raidBattleDB9999.setStage(9);
        raidBattleDB9999.setDifficulty(9);
        raidBattleDB9999.setMultiRaid(true);
        raidBattleDB9999.setMaxPlayers(999999);
        raidBattleDB9999.setRemainTime(999999999L);
        raidBattleDB9999.setActive(false);
        String rkey9999 = createStageRedisKey(raidBattleDB9999);
        raidBattleDB9999.setStageId(rkey9999);

        {
            List<String> enemyIds = new ArrayList<>();

            Optional<EnemyDB> test_monster_9999 = enemiesDBS.stream().filter(each -> each.getCharacterId().equals("TEST_MONSTER_9999")).findFirst();
            if (test_monster_9999.isPresent()) {
                enemyIds.add("TEST_MONSTER_9999");
                raidBattleDB9999.getEnemyList().add(test_monster_9999.get());
            }
            raidBattleDB9999.setEnemyIds(enemyIds);
        }

        List<RaidBattleDB> raidBattleDBS = Stream.of(raidBattleDB, raidBattleDB1, raidBattleDB2, raidBattleDB3, raidBattleDB9999).collect(Collectors.toList());

        //添加奖励
        List<ItemDB> itemsDBS = getItemDbData();
        raidBattleDBS.forEach(each -> {
            String[] list = new String[]{
                    String.valueOf(each.getArea()),
                    String.valueOf(each.getEpisode()),
                    String.valueOf(each.getChapter()),
                    String.valueOf(each.getStage()),
                    String.valueOf(each.getDifficulty()),
            };
            RewardDB rewardsDB = new RewardDB();
            Random random = new Random();
            int rand = random.nextInt(8) + 1;
            for (int i = 0; i <= rand; i++) {
                RewardDB.Item item = new RewardDB.Item();
                BeanUtils.copyProperties(itemsDBS.get(0), item);
                rewardsDB.setRewardId(each.getStageId());
                item.setAmount(random.nextInt(5) + 1);
                item.setProb(random.nextDouble());
                Collections.shuffle(itemsDBS);
                rewardsDB.getItems().add(item);
            }
            rewardsDB.makeItem();
            each.setReward(rewardsDB);
            String stageRedisKey = createStageRedisKey(list);
            raidBattleDbRepository.deleteByStageId(stageRedisKey);
            raidBattleDbDao.saveOrUpdateMap(each, stageRedisKey);
            rewardsDbRepository.deleteByRewardId(rewardsDB.getRewardId());
            rewardsDbDao.saveOrUpdateMap(rewardsDB, rewardsDB.getRewardId());
        });
    }

    private List<RaidBattleDB> getRaidBattleDB() {

        return raidBattleDbRepository.findAll();
    }

    private void InitEnemiesDB() {
        EnemyDB enemyDB = new EnemyDB();
        enemyDB.setCharacterId("TEST_MONSTER_0001");
        enemyDB.setName("测试怪物1");
        enemyDB.setBaseHp(300L);
        enemyDB.setBaseAtk(100);
        enemyDB.setBaseDef(100);

        EnemyDB enemiesDB2 = new EnemyDB();
        enemiesDB2.setCharacterId("TEST_MONSTER_0002");
        enemiesDB2.setName("测试怪物2");
        enemiesDB2.setBaseHp(999L);
        enemiesDB2.setBaseAtk(100);
        enemiesDB2.setBaseDef(100);


        EnemyDB enemiesDB3 = new EnemyDB();
        enemiesDB3.setCharacterId("TEST_MONSTER_0003");
        enemiesDB3.setName("测试怪物3");
        enemiesDB3.setBaseHp(2500L);
        enemiesDB3.setBaseAtk(100);
        enemiesDB3.setBaseDef(100);

        EnemyDB enemiesDB4 = new EnemyDB();
        enemiesDB4.setCharacterId("TEST_MONSTER_0004");
        enemiesDB4.setName("测试怪物4");
        enemiesDB4.setBaseHp(5000L);
        enemiesDB4.setBaseAtk(100);
        enemiesDB4.setBaseDef(100);

        EnemyDB enemiesDB5 = new EnemyDB();
        enemiesDB5.setCharacterId("TEST_MONSTER_0005");
        enemiesDB5.setName("测试怪物5");
        enemiesDB5.setBaseHp(15000L);
        enemiesDB5.setBaseAtk(100);
        enemiesDB5.setBaseDef(100);

        EnemyDB enemiesDB9999 = new EnemyDB();
        enemiesDB9999.setCharacterId("TEST_MONSTER_9999");
        enemiesDB9999.setName("测试怪物9999");
        enemiesDB9999.setBaseHp(100000000000L);
        enemiesDB9999.setBaseAtk(100);
        enemiesDB9999.setBaseDef(100);

        List<EnemyDB> list = Stream.of(enemyDB, enemiesDB2, enemiesDB3, enemiesDB4, enemiesDB5, enemiesDB9999).collect(Collectors.toList());

        enemyDBRepository.deleteAll();

        list.forEach(each -> {
            //enemyDBRepository.deleteByCharacterId(each.getCharacterId());
            enemyDBDao.saveOrUpdateMap(each, each.getCharacterId());
        });
    }

    private List<CharacterDB> getCharactersDB() {

        return charactersDbRepository.findAll();
    }

    private List<EnemyDB> getEnemiesDB() {

        return enemyDBRepository.findAll();
    }

    private String getRewardId(String stageId) {
        return stageId;
    }

    private void InitRewardsDB() {
        rewardsDbRepository.deleteAll();
    }

    private List<RewardDB> getRewardsDB() {
        return rewardsDbRepository.findAll();
    }

    private void InitRaidBattleEffectGroupsDB() {
        RaidBattleEffectGroupDB db1 = new RaidBattleEffectGroupDB();
        db1.setEffectGroupId("1000");//普通攻击buff
        db1.setGroupOverlapping(1);//允许重叠
        db1.setGroupMaxStackValue(1000.0d);


        RaidBattleEffectGroupDB db2 = new RaidBattleEffectGroupDB();
        db2.setEffectGroupId("1001");//普通防御buff
        db2.setGroupOverlapping(0);//只取最大值
        db2.setGroupMaxStackValue(70.0d);

        RaidBattleEffectGroupDB db3 = new RaidBattleEffectGroupDB();
        db3.setEffectGroupId("1010");//普通攻击debuff
        db3.setGroupOverlapping(1);//允许重叠
        db3.setGroupMaxStackValue(50.0d);

        RaidBattleEffectGroupDB db4 = new RaidBattleEffectGroupDB();
        db4.setEffectGroupId("1011");//普通防御debuff
        db4.setGroupOverlapping(1);//允许重叠
        db4.setGroupMaxStackValue(50.0d);

        RaidBattleEffectGroupDB db6 = new RaidBattleEffectGroupDB();
        db6.setEffectGroupId("1013");//特殊防御debuff
        db6.setGroupOverlapping(1);//允许重叠
        db6.setGroupMaxStackValue(10.0d);


        List<RaidBattleEffectGroupDB> list = Stream.of(db1, db2, db3, db4, db6).collect(Collectors.toList());

        raidBattleEffectGroupsDbRepository.deleteAll();
        list.forEach(each -> {
            raidBattleEffectGroupsDbRepository.deleteByEffectGroupId(each.getEffectGroupId());
            raidBattleEffectGroupsDbDao.saveOrUpdateMap(each, each.getEffectGroupId());
        });

    }

    private List<RaidBattleEffectGroupDB> getRaidBattleEffectGroupsDB() {
        return raidBattleEffectGroupsDbRepository.findAll();
    }

    private List<RaidBattleEffectDB> getRaidBattleEffectsDB() {
        return raidBattleEffectsDbRepository.findAll();
    }

    private void InitRaidBattleEffectsDB() {

        List<RaidBattleEffectGroupDB> raidBattleEffectGroupsDB = getRaidBattleEffectGroupsDB();

        RaidBattleEffectGroupDB group1000 = raidBattleEffectGroupsDB.stream().filter(each -> each.getEffectGroupId().equals("1000")).findFirst().get();

        RaidBattleEffectGroupDB group1001 = raidBattleEffectGroupsDB.stream().filter(each -> each.getEffectGroupId().equals("1001")).findFirst().get();

        RaidBattleEffectGroupDB group1010 = raidBattleEffectGroupsDB.stream().filter(each -> each.getEffectGroupId().equals("1010")).findFirst().get();

        RaidBattleEffectGroupDB group1011 = raidBattleEffectGroupsDB.stream().filter(each -> each.getEffectGroupId().equals("1011")).findFirst().get();

        /*RaidBattleEffectGroupsDB group2011 = raidBattleEffectGroupsDB.stream().filter(each -> each.getEffectGroupId().equals("2011")).findFirst().get();*/

        int buffPropValue = EnumCollections.DataBaseMapper.EnumNumber.RaidBattle_Effect_Prop_Buff.getValue();
        int debuffPropValue = EnumCollections.DataBaseMapper.EnumNumber.RaidBattle_Effect_Prop_Debuff.getValue();

        RaidBattleEffectDB db1 = new RaidBattleEffectDB();
        db1.setEffectId("Buff_Atk1");
        db1.setEffectGroup(group1000);
        db1.setEffectiveSecond(180);
        db1.setEffectProp(buffPropValue);
        db1.setValue1(50);

        RaidBattleEffectDB db2 = new RaidBattleEffectDB();
        db2.setEffectId("Buff_Atk2");
        db2.setEffectGroup(group1000);
        db2.setEffectiveSecond(180);
        db2.setEffectProp(buffPropValue);
        db2.setValue1(15);
        db2.setEffectMaxStack(5);

        RaidBattleEffectDB db3 = new RaidBattleEffectDB();
        db3.setEffectId("Buff_Atk3");
        db3.setEffectGroup(group1000);
        db3.setEffectiveTurn(6);
        db3.setEffectProp(buffPropValue);
        db3.setValue1(25);
        db3.setEffectMaxStack(3);

        RaidBattleEffectDB db4 = new RaidBattleEffectDB();
        db4.setEffectId("Buff_Atk4");
        db4.setEffectGroup(group1000);
        db4.setEffectProp(buffPropValue);
        db4.setValue1(10);
        db4.setEffectMaxStack(8);

        RaidBattleEffectDB db5 = new RaidBattleEffectDB();
        db5.setEffectId("Buff_Def1");
        db5.setEffectGroup(group1001);
        db5.setEffectiveSecond(180);
        db5.setEffectProp(buffPropValue);
        db5.setValue1(20);

        RaidBattleEffectDB db6 = new RaidBattleEffectDB();
        db6.setEffectId("Buff_Def2");
        db6.setEffectGroup(group1001);
        db6.setEffectiveSecond(60);
        db6.setEffectProp(buffPropValue);
        db6.setValue1(50);

        RaidBattleEffectDB db7 = new RaidBattleEffectDB();
        db7.setEffectId("Buff_Def3");
        db7.setEffectGroup(group1001);
        db7.setEffectiveTurn(2);
        db7.setEffectProp(buffPropValue);
        db7.setValue1(70);


        RaidBattleEffectDB db16 = new RaidBattleEffectDB();
        db16.setEffectId("Debuff_Def1");
        db16.setEffectGroup(group1011);
        db16.setEffectiveSecond(180);
        db16.setEffectProp(debuffPropValue);
        db16.setValue1(25);

        RaidBattleEffectDB db17 = new RaidBattleEffectDB();
        db17.setEffectId("Debuff_Def2");
        db17.setEffectGroup(group1011);
        db17.setEffectiveTurn(6);
        db17.setEffectProp(debuffPropValue);
        db17.setValue1(5);
        db17.setEffectMaxStack(4);

        RaidBattleEffectDB db18 = new RaidBattleEffectDB();
        db18.setEffectId("Debuff_Def3");
        db18.setEffectGroup(group1011);
        db18.setEffectiveTurn(6);
        db18.setEffectProp(debuffPropValue);
        db18.setValue1(5);
        db18.setEffectMaxStack(4);

        RaidBattleEffectDB db19 = new RaidBattleEffectDB();
        db19.setEffectId("Debuff_Def4");
        db19.setEffectGroup(group1011);
        db19.setEffectProp(debuffPropValue);
        db19.setValue1(10);
        db19.setEffectMaxStack(3);


        List<RaidBattleEffectDB> list = new ArrayList<>();
        list.add(db1);
        list.add(db2);
        list.add(db3);
        list.add(db4);
        list.add(db5);
        list.add(db6);
        list.add(db7);
        list.add(db16);
        list.add(db17);
        list.add(db18);
        list.add(db19);

        raidBattleEffectsDbRepository.deleteAll();

        list.forEach(each -> {
            raidBattleEffectsDbRepository.deleteByEffectId(each.getEffectId());
            raidBattleEffectsDbDao.saveOrUpdateMap(each, each.getEffectId());
        });
    }

    private List<CharacterDB> getCharactersDb() {
        return charactersDbRepository.findAll();
    }

    private void InitCharactersDb() {


        CharacterDB db1 = new CharacterDB();
        db1.setCharacterId("TEST_CHARA_0001");
        db1.setName("测试角色1001");
        db1.setLevel(1);
        db1.setBaseHp(1000L);
        db1.setBaseAtk(500);
        db1.setBaseDef(100);

        CharacterDB db2 = new CharacterDB();
        db2.setCharacterId("TEST_CHARA_0002");
        db2.setName("测试角色1002");
        db2.setLevel(1);
        db2.setBaseHp(1000L);
        db2.setBaseAtk(500);
        db2.setBaseDef(100);

        CharacterDB db3 = new CharacterDB();
        db3.setCharacterId("TEST_CHARA_0003");
        db3.setName("测试角色1003");
        db3.setLevel(1);
        db3.setBaseHp(1000L);
        db3.setBaseAtk(500);
        db3.setBaseDef(100);

        CharacterDB db4 = new CharacterDB();
        db4.setCharacterId("TEST_CHARA_0004");
        db4.setName("测试角色1004");
        db4.setLevel(1);
        db4.setBaseHp(1000L);
        db4.setBaseAtk(500);
        db4.setBaseDef(100);


        List<CharacterDB> list = Stream.of(db1, db2, db3, db4).collect(Collectors.toList());

        charactersDbRepository.deleteAll();

        list.forEach(each -> {
            //charactersDbRepository.deleteByCharacterId(each.getCharacterId());
            charactersDbDao.saveOrUpdateMap(each, each.getCharacterId());
        });
    }

    private void InitGachaPoolsDb() {

        List<CharacterDB> charactersDb = getCharactersDb();

        CharacterDB test_chara_0001 = charactersDb.stream().filter(each -> each.getCharacterId().equals("TEST_CHARA_0001")).findFirst().get();
        CharacterDB test_chara_0002 = charactersDb.stream().filter(each -> each.getCharacterId().equals("TEST_CHARA_0002")).findFirst().get();
        CharacterDB test_chara_0003 = charactersDb.stream().filter(each -> each.getCharacterId().equals("TEST_CHARA_0003")).findFirst().get();
        CharacterDB test_chara_0004 = charactersDb.stream().filter(each -> each.getCharacterId().equals("TEST_CHARA_0004")).findFirst().get();


        GachaPoolDB db1 = new GachaPoolDB();
        db1.setGachaPoolId("GachaPoolAlpha0001");
        db1.setActive(true);

        GachaPoolDB.GachaPoolCharacter chara1 = new GachaPoolDB.GachaPoolCharacter();
        chara1.setCharacterId(test_chara_0001.getCharacterId());
        chara1.setProb(0.15);

        GachaPoolDB.GachaPoolCharacter chara2 = new GachaPoolDB.GachaPoolCharacter();
        chara2.setCharacterId(test_chara_0002.getCharacterId());
        chara2.setProb(0.4);

        GachaPoolDB.GachaPoolCharacter chara3 = new GachaPoolDB.GachaPoolCharacter();
        chara3.setCharacterId(test_chara_0003.getCharacterId());
        chara3.setProb(0.25);

        GachaPoolDB.GachaPoolCharacter chara4 = new GachaPoolDB.GachaPoolCharacter();
        chara4.setCharacterId(test_chara_0004.getCharacterId());
        chara4.setProb(0.2);

        List<GachaPoolDB.GachaPoolCharacter> list = Stream.of(chara1, chara2, chara3, chara4).collect(Collectors.toList());

        db1.setCharacters(list);

        gachaPoolsDbRepository.deleteAll();

        gachaPoolsDbRepository.deleteById(db1.getGachaPoolId());
        gachaPoolsDbDao.saveOrUpdateMap(db1, db1.getGachaPoolId());

    }


    private void InitTasksDb() {
        List<ItemDB> itemsDBS = getItemDbData();

        String task1Id = "Task000001";

        String task2Id = "Task000002";

        String task3Id = "Task000003";

        String task4Id = "Task000004";

        String task5Id = "Task000005";

        String task6Id = "Task000006";

        String task7Id = "Task000007";

        String task8Id = "Task000008";

        String task9Id = "Task000009";


        TaskDB<ConsumeGoldTask> task1 = new TaskDB<>();
        {
            RewardDB rewardsDB = new RewardDB();
            RewardDB.Item item = new RewardDB.Item();
            BeanUtils.copyProperties(itemsDBS.stream().filter(it -> it.getItemId().equals("1")).findFirst().get(), item);
            item.setAmount(1);
            rewardsDB.setRewardId(task1Id);
            rewardsDB.getItems().add(item);
            rewardsDB.makeItem();
            rewardsDbRepository.deleteByRewardId(rewardsDB.getRewardId());
            rewardsDbDao.saveOrUpdateMap(rewardsDB, rewardsDB.getRewardId());

            task1.setTaskId(task1Id);
            task1.setRewardsDB(rewardsDB);
            ConsumeGoldTask cgt = new ConsumeGoldTask();
            task1.setTaskEntity(cgt);
            task1.setTaskType(TaskEnumCollections.EnumTaskType.ConsumeGold.getType());
            cgt.setQuota(100);
        }


        TaskDB<ConsumeGoldTask> task2 = new TaskDB<>();
        {
            RewardDB rewardsDB = new RewardDB();
            RewardDB.Item item = new RewardDB.Item();
            BeanUtils.copyProperties(itemsDBS.stream().filter(it -> it.getItemId().equals("1")).findFirst().get(), item);
            item.setAmount(5);
            rewardsDB.setRewardId(task2Id);
            rewardsDB.getItems().add(item);
            rewardsDB.makeItem();
            rewardsDbRepository.deleteByRewardId(rewardsDB.getRewardId());
            rewardsDbDao.saveOrUpdateMap(rewardsDB, rewardsDB.getRewardId());

            task2.setTaskId(task2Id);
            task2.setRewardsDB(rewardsDB);
            ConsumeGoldTask cgt = new ConsumeGoldTask();
            task2.setTaskType(TaskEnumCollections.EnumTaskType.ConsumeGold.getType());
            task2.setTaskEntity(cgt);
            cgt.setQuota(1000);
        }

        TaskDB<ConsumeGoldTask> task3 = new TaskDB<>();
        {
            RewardDB rewardsDB = new RewardDB();
            RewardDB.Item item = new RewardDB.Item();
            BeanUtils.copyProperties(itemsDBS.stream().filter(it -> it.getItemId().equals("1")).findFirst().get(), item);
            item.setAmount(10);
            rewardsDB.setRewardId(task3Id);
            rewardsDB.getItems().add(item);
            rewardsDB.makeItem();
            rewardsDbRepository.deleteByRewardId(rewardsDB.getRewardId());
            rewardsDbDao.saveOrUpdateMap(rewardsDB, rewardsDB.getRewardId());

            task3.setTaskId(task3Id);
            task3.setRewardsDB(rewardsDB);
            ConsumeGoldTask cgt = new ConsumeGoldTask();
            task3.setTaskType(TaskEnumCollections.EnumTaskType.ConsumeGold.getType());
            task3.setTaskEntity(cgt);
            cgt.setQuota(10000);
        }

        TaskDB<ConsumeGoldTask> task4 = new TaskDB<>();
        {
            final String id = task4Id;
            RewardDB rewardsDB = new RewardDB();
            RewardDB.Item item = new RewardDB.Item();
            BeanUtils.copyProperties(itemsDBS.stream().filter(it -> it.getItemId().equals("1")).findFirst().get(), item);
            item.setAmount(10);
            rewardsDB.setRewardId(id);
            rewardsDB.getItems().add(item);
            rewardsDB.makeItem();
            rewardsDbRepository.deleteByRewardId(rewardsDB.getRewardId());
            rewardsDbDao.saveOrUpdateMap(rewardsDB, rewardsDB.getRewardId());

            task4.setTaskId(id);
            task4.setRewardsDB(rewardsDB);
            ConsumeGoldTask cgt = new ConsumeGoldTask();
            task4.setTaskType(TaskEnumCollections.EnumTaskType.ConsumeGold.getType());
            task4.setTaskEntity(cgt);
            cgt.setQuota(30000);
        }

        TaskDB<DayFirstLoginTask> task5 = new TaskDB<>();
        {
            final String id = task5Id;
            RewardDB rewardsDB = new RewardDB();
            RewardDB.Item item = new RewardDB.Item();
            BeanUtils.copyProperties(itemsDBS.stream().filter(it -> it.getItemId().equals("1000")).findFirst().get(), item);
            item.setAmount(10);
            rewardsDB.setRewardId(id);
            rewardsDB.getItems().add(item);
            rewardsDB.makeItem();
            rewardsDbRepository.deleteByRewardId(rewardsDB.getRewardId());
            rewardsDbDao.saveOrUpdateMap(rewardsDB, rewardsDB.getRewardId());

            task5.setTaskId(id);
            task5.setRewardsDB(rewardsDB);
            DayFirstLoginTask cgt = new DayFirstLoginTask();
            task5.setTaskType(TaskEnumCollections.EnumTaskType.DayFirstLogin.getType());
            task5.setRefreshType(EnumCollections.DataBaseMapper.EnumNumber.Tomorrow_05_Refresh.getValue());
            task5.setTaskEntity(cgt);
            cgt.setQuota(1);
        }

        TaskDB<SpecificStagePassTimesTask> task6 = new TaskDB<>();
        {

            final String id = task6Id;
            String[] r1 = new String[]{"1", "1", "1", "1", "1"};
            String stageId = createStageRedisKey(r1);

            RewardDB rewardsDB = new RewardDB();
            RewardDB.Item item = new RewardDB.Item();
            BeanUtils.copyProperties(itemsDBS.stream().filter(it -> it.getItemId().equals("1000")).findFirst().get(), item);
            item.setAmount(10);
            rewardsDB.setRewardId(id);
            rewardsDB.getItems().add(item);
            rewardsDB.makeItem();
            rewardsDbRepository.deleteByRewardId(rewardsDB.getRewardId());
            rewardsDbDao.saveOrUpdateMap(rewardsDB, rewardsDB.getRewardId());

            task6.setTaskId(id);
            task6.setRewardsDB(rewardsDB);
            SpecificStagePassTimesTask cgt = new SpecificStagePassTimesTask();
            task6.setTaskType(TaskEnumCollections.EnumTaskType.StagePassTimes.getType());
            task6.setTaskEntity(cgt);
            cgt.setQuota(5);
            cgt.setStageId(stageId);
        }

        TaskDB<DayFirstLoginTask> task7 = new TaskDB<>();
        {

            final String id = task7Id;
            RewardDB rewardsDB = new RewardDB();
            RewardDB.Item item = new RewardDB.Item();
            BeanUtils.copyProperties(itemsDBS.stream().filter(it -> it.getItemId().equals("1")).findFirst().get(), item);
            item.setAmount(5);
            rewardsDB.setRewardId(id);
            rewardsDB.getItems().add(item);
            rewardsDB.makeItem();
            rewardsDbRepository.deleteByRewardId(rewardsDB.getRewardId());
            rewardsDbDao.saveOrUpdateMap(rewardsDB, rewardsDB.getRewardId());

            task7.setTaskId(id);
            task7.setRewardsDB(rewardsDB);
            DayFirstLoginTask cgt = new DayFirstLoginTask();
            task7.setTaskType(TaskEnumCollections.EnumTaskType.DayFirstLogin.getType());
            task7.setRefreshType(EnumCollections.DataBaseMapper.EnumNumber.Tomorrow_05_Refresh.getValue());
            task7.setTaskEntity(cgt);
            task7.setCondition("#hour >= 12");
            cgt.setQuota(1);
        }

        TaskDB<DayFirstLoginTask> task8 = new TaskDB<>();
        {

            final String id = task8Id;
            RewardDB rewardsDB = new RewardDB();
            RewardDB.Item item = new RewardDB.Item();
            BeanUtils.copyProperties(itemsDBS.stream().filter(it -> it.getItemId().equals("1000")).findFirst().get(), item);
            item.setAmount(10);
            rewardsDB.setRewardId(id);
            rewardsDB.getItems().add(item);
            rewardsDB.makeItem();
            rewardsDbRepository.deleteByRewardId(rewardsDB.getRewardId());
            rewardsDbDao.saveOrUpdateMap(rewardsDB, rewardsDB.getRewardId());

            task8.setTaskId(id);
            task8.setRewardsDB(rewardsDB);
            DayFirstLoginTask cgt = new DayFirstLoginTask();
            task8.setTaskType(TaskEnumCollections.EnumTaskType.DayFirstLogin.getType());
            task8.setRefreshType(EnumCollections.DataBaseMapper.EnumNumber.Tomorrow_05_Refresh.getValue());
            task8.setTaskEntity(cgt);
            task8.setCondition("#hour >= 18");
            cgt.setQuota(1);
        }


        tasksDbRepository.deleteAll();
        Stream.of(task1, task2, task3, task4, task5, task6, task7, task8).forEach(each -> {
            tasksDbRepository.deleteById(each.getTaskId());
            tasksDbDao.saveOrUpdateMap(each, each.getTaskId());
        });
    }

}
