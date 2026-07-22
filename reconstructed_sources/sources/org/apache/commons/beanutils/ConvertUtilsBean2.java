package org.apache.commons.beanutils;

/* loaded from: classes12.dex */
public class ConvertUtilsBean2 extends ConvertUtilsBean {
    @Override // org.apache.commons.beanutils.ConvertUtilsBean
    public String convert(Object value) {
        return (String) convert(value, String.class);
    }

    @Override // org.apache.commons.beanutils.ConvertUtilsBean
    public Object convert(String value, Class<?> clazz) {
        return convert((Object) value, clazz);
    }

    @Override // org.apache.commons.beanutils.ConvertUtilsBean
    public Object convert(String[] value, Class<?> clazz) {
        return convert((Object) value, clazz);
    }
}
