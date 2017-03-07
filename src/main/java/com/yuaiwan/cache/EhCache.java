package com.yuaiwan.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import com.yuaiwan.utils.SerializeUtil;

/**
 * 缓存的数据源,该类中所有的方法在没有缓存的时候会返回null,系统仍能够正常运行
 * @author guangyu
 */
public class EhCache implements Cache {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
     * Redis
     */
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存名称
     */
    private String name;

    /**
     * 超时时间
     */
    private long timeout;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this.redisTemplate;
    }

    @Override
    public ValueWrapper get(Object key) {
        try {
			if (StringUtils.isEmpty(key)) {
			    return null;
			} else {
			    final String finalKey;
			    if (key instanceof String) {
			        finalKey = (String) key;
			    } else {
			        finalKey = key.toString();
			    }
			    Object object = null;
			    object = redisTemplate.execute(new RedisCallback<Object>() {
			        public Object doInRedis(RedisConnection connection) throws DataAccessException {
			            byte[] key = finalKey.getBytes();
			            byte[] value = connection.get(key);
			            if (value == null) {
			                return null;
			            }
			            return SerializeUtil.unserialize(value);
			        }
			    });
			    return (object != null ? new SimpleValueWrapper(object) : null);
			}
		} catch (Exception e) {
			logger.error("redis错误",e);
			return null;
		}
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T get(Object key, Class<T> type) {
        try {
			if (StringUtils.isEmpty(key) || null == type) {
			    return null;
			} else {
			    final String finalKey;
			    final Class<T> finalType = type;
			    if (key instanceof String) {
			        finalKey = (String) key;
			    } else {
			        finalKey = key.toString();
			    }
			    final Object object = redisTemplate.execute(new RedisCallback<Object>() {
			        public Object doInRedis(RedisConnection connection) throws DataAccessException {
			            byte[] key = finalKey.getBytes();
			            byte[] value = connection.get(key);
			            if (value == null) {
			                return null;
			            }
			            return SerializeUtil.unserialize(value);
			        }
			    });
			    if (finalType != null && finalType.isInstance(object) && null != object) {
			        return (T) object;
			    } else {
			        return null;
			    }
			}
		} catch (Exception e) {
			logger.error("redis错误",e);
			return null;
		}
    }

    @Override
    public void put(Object key,Object value) {
		try {
			if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
			    return;
			} else {
			    final String finalKey;
			    if (key instanceof String) {
			        finalKey = (String) key;
			    } else {
			        finalKey = key.toString();
			    }
			    if (!StringUtils.isEmpty(finalKey)) {
			        final Object finalValue = value;
			        redisTemplate.execute(new RedisCallback<Boolean>() {
			            @Override
			            public Boolean doInRedis(RedisConnection connection) {
			                connection.set(finalKey.getBytes(), SerializeUtil.serialize(finalValue));
			                // 设置超时间
			                connection.expire(finalKey.getBytes(), timeout);
			                return true;
			            }
			        });
			    }
			}
		} catch (Exception e) {
			logger.error("redis错误",e);
		}
    }

    /*
     * 根据Key 删除缓存
     */
    @Override
    public void evict(Object key) {
        try {
			if (null != key) {
			    final String finalKey;
			    if (key instanceof String) {
			        finalKey = (String) key;
			    } else {
			        finalKey = key.toString();
			    }
			    if (!StringUtils.isEmpty(finalKey)) {
			        redisTemplate.execute(new RedisCallback<Long>() {
			            public Long doInRedis(RedisConnection connection) throws DataAccessException {
			                return connection.del(finalKey.getBytes());
			            }
			        });
			    }
			}
		} catch (Exception e) {
			logger.error("redis错误",e);
		}
    }

    /*
     * 清楚系统缓存
     */
    @Override
    public void clear() {
		try {
			redisTemplate.execute(new RedisCallback<String>() {
				public String doInRedis(RedisConnection connection)throws DataAccessException {
					connection.flushDb();
					return "ok";
				}
			});
		} catch (Exception e) {
			logger.error("redis错误",e);
		}
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		 try {
			put(key, value);
			return new SimpleValueWrapper(value);
		} catch (Exception e) {
			logger.error("redis错误",e);
			return null;
		}  
	}
}
