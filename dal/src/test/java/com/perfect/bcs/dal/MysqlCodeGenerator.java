package com.perfect.bcs.dal;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;

/**
 * @author liangbo 梁波
 * @date 2020-02-04 13:49
 */
public class MysqlCodeGenerator {

    private static String ipPort = "rm-0jl68sso55d29x3j7to.mysql.rds.aliyuncs.com";
    private static String dbName = "bcs";
    private static String dbUserName = "systemadmin";
    private static String dbPassword = "##@##HSBCyuyu34uKfd";
    // 配置代码生成路径
    private static String sourceCodePath = "/dal/src/main/java";
    // 修改包的路径
    private static String packagePath = "com.perfect.bcs.dal";

    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        // 配置代码生成路径
        gc.setOutputDir(projectPath + sourceCodePath);
        gc.setAuthor("MyBatis-Plus自动生成");
        // 设置不需要生成文件目录后自动打开： false 不需要
        gc.setOpen(false);
        // 设置是否覆盖已经生成的文件，true 覆盖
        gc.setFileOverride(true);
        gc.setActiveRecord(true);
        // 设置时间类型：关系到生成实体类时时间字段的类型
        gc.setDateType(DateType.ONLY_DATE);
        gc.setEntityName("%sDO");
        gc.setMapperName("%sMapper");
        // 设置 id为自增类型
        gc.setIdType(IdType.AUTO);
        gc.setBaseResultMap(true);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        String dbUrl = "jdbc:mysql://" + ipPort
            + "/" + dbName
            + "?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&serverTimezone=Asia/Shanghai";
        dsc.setUrl(dbUrl);
        // dsc.setSchemaName("public");
        String dbDriverName = "com.mysql.cj.jdbc.Driver";
        dsc.setDriverName(dbDriverName);
        dsc.setUsername(dbUserName);
        dsc.setPassword(dbPassword);
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        // 不需要设置模块名
        //String moduleName = scanner("请输入模块名：");
        //pc.setModuleName(moduleName);
        pc.setParent(packagePath);
        // 修改包名
        pc.setEntity("domain");
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        // 不需要生成 xml
        templateConfig.setXml(null);

        // 不生成 Service 和 ServiceImpl
        templateConfig.setService(null);
        templateConfig.setServiceImpl(null);
        // 不生成 Controller
        templateConfig.setController(null);

        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setSuperEntityClass("com.baomidou.mybatisplus.extension.activerecord.Model");
        strategy.setEntityLombokModel(true);//【实体】是否为lombok模型
        strategy.setChainModel(true);//链式编码

        // 不需要 controller
        // strategy.setRestControllerStyle(true);
        // 公共父类，如果没有父类控制器,就不用设置!
        //strategy.setSuperControllerClass("FatherController");
        // 写于父类中的公共字段，实体类没有父类，不需要设置
        //strategy.setSuperEntityColumns("id");
        String tableStr = scanner("请输入表名；如果多个，请使用英文逗号分割！");
        String[] tables = StringUtils.split(tableStr, ",");
        String[] tableArray = new String[tables.length];
        for (int i = 0; i < tables.length; i++) {
            tableArray[i] = StringUtils.trimToEmpty(tables[i]);
        }
        strategy.setInclude(tableArray);
        strategy.setControllerMappingHyphenStyle(true);
        //表名前缀，如果设置，那么这样生成的bean里面 就没有下面的前缀了
        //strategy.setTablePrefix(pc.getModuleName() + "_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }
}
