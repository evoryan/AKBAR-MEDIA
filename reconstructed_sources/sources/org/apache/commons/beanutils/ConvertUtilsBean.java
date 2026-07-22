package org.apache.commons.beanutils;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.BigIntegerConverter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.ByteConverter;
import org.apache.commons.beanutils.converters.CalendarConverter;
import org.apache.commons.beanutils.converters.CharacterConverter;
import org.apache.commons.beanutils.converters.ClassConverter;
import org.apache.commons.beanutils.converters.ConverterFacade;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FileConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortConverter;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.beanutils.converters.SqlTimeConverter;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.apache.commons.beanutils.converters.URLConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/* loaded from: classes12.dex */
public class ConvertUtilsBean {
    private static final Integer ZERO = new Integer(0);
    private static final Character SPACE = new Character(' ');

    @Deprecated
    private static Short defaultShort = new Short((short) 0);
    private final WeakFastHashMap<Class<?>, Converter> converters = new WeakFastHashMap<>();
    private final Log log = LogFactory.getLog(ConvertUtils.class);

    @Deprecated
    private Boolean defaultBoolean = Boolean.FALSE;

    @Deprecated
    private Byte defaultByte = new Byte((byte) 0);

    @Deprecated
    private Character defaultCharacter = new Character(' ');

    @Deprecated
    private Double defaultDouble = new Double(0.0d);

    @Deprecated
    private Float defaultFloat = new Float(0.0f);

    @Deprecated
    private Integer defaultInteger = new Integer(0);

    @Deprecated
    private Long defaultLong = new Long(0);

    /* JADX INFO: Access modifiers changed from: protected */
    public static ConvertUtilsBean getInstance() {
        return BeanUtilsBean.getInstance().getConvertUtils();
    }

    public ConvertUtilsBean() {
        this.converters.setFast(false);
        deregister();
        this.converters.setFast(true);
    }

    @Deprecated
    public boolean getDefaultBoolean() {
        return this.defaultBoolean.booleanValue();
    }

    @Deprecated
    public void setDefaultBoolean(boolean newDefaultBoolean) {
        this.defaultBoolean = newDefaultBoolean ? Boolean.TRUE : Boolean.FALSE;
        register(new BooleanConverter(this.defaultBoolean), Boolean.TYPE);
        register(new BooleanConverter(this.defaultBoolean), Boolean.class);
    }

    @Deprecated
    public byte getDefaultByte() {
        return this.defaultByte.byteValue();
    }

    @Deprecated
    public void setDefaultByte(byte newDefaultByte) {
        this.defaultByte = new Byte(newDefaultByte);
        register(new ByteConverter(this.defaultByte), Byte.TYPE);
        register(new ByteConverter(this.defaultByte), Byte.class);
    }

    @Deprecated
    public char getDefaultCharacter() {
        return this.defaultCharacter.charValue();
    }

    @Deprecated
    public void setDefaultCharacter(char newDefaultCharacter) {
        this.defaultCharacter = new Character(newDefaultCharacter);
        register(new CharacterConverter(this.defaultCharacter), Character.TYPE);
        register(new CharacterConverter(this.defaultCharacter), Character.class);
    }

    @Deprecated
    public double getDefaultDouble() {
        return this.defaultDouble.doubleValue();
    }

    @Deprecated
    public void setDefaultDouble(double newDefaultDouble) {
        this.defaultDouble = new Double(newDefaultDouble);
        register(new DoubleConverter(this.defaultDouble), Double.TYPE);
        register(new DoubleConverter(this.defaultDouble), Double.class);
    }

    @Deprecated
    public float getDefaultFloat() {
        return this.defaultFloat.floatValue();
    }

    @Deprecated
    public void setDefaultFloat(float newDefaultFloat) {
        this.defaultFloat = new Float(newDefaultFloat);
        register(new FloatConverter(this.defaultFloat), Float.TYPE);
        register(new FloatConverter(this.defaultFloat), Float.class);
    }

    @Deprecated
    public int getDefaultInteger() {
        return this.defaultInteger.intValue();
    }

    @Deprecated
    public void setDefaultInteger(int newDefaultInteger) {
        this.defaultInteger = new Integer(newDefaultInteger);
        register(new IntegerConverter(this.defaultInteger), Integer.TYPE);
        register(new IntegerConverter(this.defaultInteger), Integer.class);
    }

    @Deprecated
    public long getDefaultLong() {
        return this.defaultLong.longValue();
    }

    @Deprecated
    public void setDefaultLong(long newDefaultLong) {
        this.defaultLong = new Long(newDefaultLong);
        register(new LongConverter(this.defaultLong), Long.TYPE);
        register(new LongConverter(this.defaultLong), Long.class);
    }

    @Deprecated
    public short getDefaultShort() {
        return defaultShort.shortValue();
    }

    @Deprecated
    public void setDefaultShort(short newDefaultShort) {
        defaultShort = new Short(newDefaultShort);
        register(new ShortConverter(defaultShort), Short.TYPE);
        register(new ShortConverter(defaultShort), Short.class);
    }

    public String convert(Object value) {
        Object value2;
        if (value == null) {
            return null;
        }
        if (value.getClass().isArray()) {
            if (Array.getLength(value) < 1 || (value2 = Array.get(value, 0)) == null) {
                return null;
            }
            Converter converter = lookup(String.class);
            return (String) converter.convert(String.class, value2);
        }
        Converter converter2 = lookup(String.class);
        return (String) converter2.convert(String.class, value);
    }

    public Object convert(String value, Class<?> clazz) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Convert string '" + value + "' to class '" + clazz.getName() + "'");
        }
        Converter converter = lookup(clazz);
        if (converter == null) {
            converter = lookup(String.class);
        }
        if (this.log.isTraceEnabled()) {
            this.log.trace("  Using converter " + converter);
        }
        return converter.convert(clazz, value);
    }

    public Object convert(String[] values, Class<?> clazz) {
        Class<?> type = clazz;
        if (clazz.isArray()) {
            type = clazz.getComponentType();
        }
        if (this.log.isDebugEnabled()) {
            this.log.debug("Convert String[" + values.length + "] to class '" + type.getName() + "[]'");
        }
        Converter converter = lookup(type);
        if (converter == null) {
            converter = lookup(String.class);
        }
        if (this.log.isTraceEnabled()) {
            this.log.trace("  Using converter " + converter);
        }
        Object array = Array.newInstance(type, values.length);
        for (int i = 0; i < values.length; i++) {
            Array.set(array, i, converter.convert(type, values[i]));
        }
        return array;
    }

    public Object convert(Object value, Class<?> targetType) {
        Class<?> sourceType = value == null ? null : value.getClass();
        if (this.log.isDebugEnabled()) {
            Log log = this.log;
            if (value == null) {
                log.debug("Convert null value to type '" + targetType.getName() + "'");
            } else {
                log.debug("Convert type '" + sourceType.getName() + "' value '" + value + "' to type '" + targetType.getName() + "'");
            }
        }
        Object converted = value;
        Converter converter = lookup(sourceType, targetType);
        if (converter != null) {
            if (this.log.isTraceEnabled()) {
                this.log.trace("  Using converter " + converter);
            }
            converted = converter.convert(targetType, value);
        }
        if (String.class.equals(targetType) && converted != null && !(converted instanceof String)) {
            Converter converter2 = lookup(String.class);
            if (converter2 != null) {
                if (this.log.isTraceEnabled()) {
                    this.log.trace("  Using converter " + converter2);
                }
                converted = converter2.convert(String.class, converted);
            }
            if (converted != null && !(converted instanceof String)) {
                return converted.toString();
            }
            return converted;
        }
        return converted;
    }

    public void deregister() {
        this.converters.clear();
        registerPrimitives(false);
        registerStandard(false, false);
        registerOther(true);
        registerArrays(false, 0);
        register(BigDecimal.class, new BigDecimalConverter());
        register(BigInteger.class, new BigIntegerConverter());
    }

    public void register(boolean throwException, boolean defaultNull, int defaultArraySize) {
        registerPrimitives(throwException);
        registerStandard(throwException, defaultNull);
        registerOther(throwException);
        registerArrays(throwException, defaultArraySize);
    }

    private void registerPrimitives(boolean throwException) {
        register(Boolean.TYPE, throwException ? new BooleanConverter() : new BooleanConverter(Boolean.FALSE));
        register(Byte.TYPE, throwException ? new ByteConverter() : new ByteConverter(ZERO));
        register(Character.TYPE, throwException ? new CharacterConverter() : new CharacterConverter(SPACE));
        register(Double.TYPE, throwException ? new DoubleConverter() : new DoubleConverter(ZERO));
        register(Float.TYPE, throwException ? new FloatConverter() : new FloatConverter(ZERO));
        register(Integer.TYPE, throwException ? new IntegerConverter() : new IntegerConverter(ZERO));
        register(Long.TYPE, throwException ? new LongConverter() : new LongConverter(ZERO));
        register(Short.TYPE, throwException ? new ShortConverter() : new ShortConverter(ZERO));
    }

    private void registerStandard(boolean throwException, boolean defaultNull) {
        Number defaultNumber = defaultNull ? null : ZERO;
        BigDecimal bigDecDeflt = defaultNull ? null : new BigDecimal("0.0");
        BigInteger bigIntDeflt = defaultNull ? null : new BigInteger("0");
        Boolean booleanDefault = defaultNull ? null : Boolean.FALSE;
        Character charDefault = defaultNull ? null : SPACE;
        String stringDefault = defaultNull ? null : "";
        register(BigDecimal.class, throwException ? new BigDecimalConverter() : new BigDecimalConverter(bigDecDeflt));
        register(BigInteger.class, throwException ? new BigIntegerConverter() : new BigIntegerConverter(bigIntDeflt));
        register(Boolean.class, throwException ? new BooleanConverter() : new BooleanConverter(booleanDefault));
        register(Byte.class, throwException ? new ByteConverter() : new ByteConverter(defaultNumber));
        register(Character.class, throwException ? new CharacterConverter() : new CharacterConverter(charDefault));
        register(Double.class, throwException ? new DoubleConverter() : new DoubleConverter(defaultNumber));
        register(Float.class, throwException ? new FloatConverter() : new FloatConverter(defaultNumber));
        register(Integer.class, throwException ? new IntegerConverter() : new IntegerConverter(defaultNumber));
        register(Long.class, throwException ? new LongConverter() : new LongConverter(defaultNumber));
        register(Short.class, throwException ? new ShortConverter() : new ShortConverter(defaultNumber));
        register(String.class, throwException ? new StringConverter() : new StringConverter(stringDefault));
    }

    private void registerOther(boolean throwException) {
        register(Class.class, throwException ? new ClassConverter() : new ClassConverter(null));
        register(Date.class, throwException ? new DateConverter() : new DateConverter(null));
        register(Calendar.class, throwException ? new CalendarConverter() : new CalendarConverter(null));
        register(File.class, throwException ? new FileConverter() : new FileConverter(null));
        register(java.sql.Date.class, throwException ? new SqlDateConverter() : new SqlDateConverter(null));
        register(Time.class, throwException ? new SqlTimeConverter() : new SqlTimeConverter(null));
        register(Timestamp.class, throwException ? new SqlTimestampConverter() : new SqlTimestampConverter(null));
        register(URL.class, throwException ? new URLConverter() : new URLConverter(null));
    }

    private void registerArrays(boolean throwException, int defaultArraySize) {
        registerArrayConverter(Boolean.TYPE, new BooleanConverter(), throwException, defaultArraySize);
        registerArrayConverter(Byte.TYPE, new ByteConverter(), throwException, defaultArraySize);
        registerArrayConverter(Character.TYPE, new CharacterConverter(), throwException, defaultArraySize);
        registerArrayConverter(Double.TYPE, new DoubleConverter(), throwException, defaultArraySize);
        registerArrayConverter(Float.TYPE, new FloatConverter(), throwException, defaultArraySize);
        registerArrayConverter(Integer.TYPE, new IntegerConverter(), throwException, defaultArraySize);
        registerArrayConverter(Long.TYPE, new LongConverter(), throwException, defaultArraySize);
        registerArrayConverter(Short.TYPE, new ShortConverter(), throwException, defaultArraySize);
        registerArrayConverter(BigDecimal.class, new BigDecimalConverter(), throwException, defaultArraySize);
        registerArrayConverter(BigInteger.class, new BigIntegerConverter(), throwException, defaultArraySize);
        registerArrayConverter(Boolean.class, new BooleanConverter(), throwException, defaultArraySize);
        registerArrayConverter(Byte.class, new ByteConverter(), throwException, defaultArraySize);
        registerArrayConverter(Character.class, new CharacterConverter(), throwException, defaultArraySize);
        registerArrayConverter(Double.class, new DoubleConverter(), throwException, defaultArraySize);
        registerArrayConverter(Float.class, new FloatConverter(), throwException, defaultArraySize);
        registerArrayConverter(Integer.class, new IntegerConverter(), throwException, defaultArraySize);
        registerArrayConverter(Long.class, new LongConverter(), throwException, defaultArraySize);
        registerArrayConverter(Short.class, new ShortConverter(), throwException, defaultArraySize);
        registerArrayConverter(String.class, new StringConverter(), throwException, defaultArraySize);
        registerArrayConverter(Class.class, new ClassConverter(), throwException, defaultArraySize);
        registerArrayConverter(Date.class, new DateConverter(), throwException, defaultArraySize);
        registerArrayConverter(Calendar.class, new DateConverter(), throwException, defaultArraySize);
        registerArrayConverter(File.class, new FileConverter(), throwException, defaultArraySize);
        registerArrayConverter(java.sql.Date.class, new SqlDateConverter(), throwException, defaultArraySize);
        registerArrayConverter(Time.class, new SqlTimeConverter(), throwException, defaultArraySize);
        registerArrayConverter(Timestamp.class, new SqlTimestampConverter(), throwException, defaultArraySize);
        registerArrayConverter(URL.class, new URLConverter(), throwException, defaultArraySize);
    }

    private void registerArrayConverter(Class<?> componentType, Converter componentConverter, boolean throwException, int defaultArraySize) {
        Converter arrayConverter;
        Class<?> arrayType = Array.newInstance(componentType, 0).getClass();
        if (throwException) {
            arrayConverter = new ArrayConverter(arrayType, componentConverter);
        } else {
            arrayConverter = new ArrayConverter(arrayType, componentConverter, defaultArraySize);
        }
        register(arrayType, arrayConverter);
    }

    private void register(Class<?> clazz, Converter converter) {
        register(new ConverterFacade(converter), clazz);
    }

    public void deregister(Class<?> clazz) {
        this.converters.remove(clazz);
    }

    public Converter lookup(Class<?> clazz) {
        return this.converters.get(clazz);
    }

    public Converter lookup(Class<?> sourceType, Class<?> targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type is missing");
        }
        if (sourceType == null) {
            return lookup(targetType);
        }
        Converter converter = null;
        if (targetType == String.class) {
            Converter converter2 = lookup(sourceType);
            if (converter2 == null && (sourceType.isArray() || Collection.class.isAssignableFrom(sourceType))) {
                converter2 = lookup(String[].class);
            }
            if (converter2 == null) {
                Converter converter3 = lookup(String.class);
                return converter3;
            }
            return converter2;
        }
        if (targetType == String[].class) {
            if (sourceType.isArray() || Collection.class.isAssignableFrom(sourceType)) {
                converter = lookup(sourceType);
            }
            if (converter == null) {
                Converter converter4 = lookup(String[].class);
                return converter4;
            }
            return converter;
        }
        return lookup(targetType);
    }

    public void register(Converter converter, Class<?> clazz) {
        this.converters.put(clazz, converter);
    }
}
