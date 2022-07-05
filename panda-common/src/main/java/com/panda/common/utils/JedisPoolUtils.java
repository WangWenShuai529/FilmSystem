package com.panda.common.utils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 JedisPool������
 ���������ļ����������ӳصĲ���
 �ṩ��ȡ���ӵķ���

 */
public class JedisPoolUtils {

    private static JedisPool jedisPool;

    static{
        //��ȡ�����ļ�
        InputStream is = JedisPoolUtils.class.getClassLoader().getResourceAsStream("jedis.properties");
        //����Properties����
        Properties pro = new Properties();
        //�����ļ�
        try {
            pro.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //��ȡ���ݣ����õ�JedisPoolConfig��
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.parseInt(pro.getProperty("maxTotal")));
        config.setMaxIdle(Integer.parseInt(pro.getProperty("maxIdle")));

        //��ʼ��JedisPool
        jedisPool = new JedisPool(config,pro.getProperty("host"),Integer.parseInt(pro.getProperty("port")));
    }


    /**
     * ��ȡ���ӷ���
     */
    public static Jedis getJedis(){
        return jedisPool.getResource();
    }
}
