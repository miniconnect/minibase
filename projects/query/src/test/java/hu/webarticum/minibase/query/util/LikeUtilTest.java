package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LikeUtilTest {

    @Test
    void testBuildRegexString() {
        assertThat(LikeUtil.buildRegexString("", null)).isEqualTo("^$");
        assertThat(LikeUtil.buildRegexString("_", null)).isEqualTo("^.$");
        assertThat(LikeUtil.buildRegexString("lorem%", null)).isEqualTo("^lorem.*$");
        assertThat(LikeUtil.buildRegexString("%ipsum", null)).isEqualTo("^.*ipsum$");
        assertThat(LikeUtil.buildRegexString("d%ol__o_r%", null)).isEqualTo("^d.*ol..o.r.*$");
        assertThat(LikeUtil.buildRegexString("s(i)?t*\\%", null)).isEqualTo("^s\\(i\\)\\?t\\*\\\\.*$");
        assertThat(LikeUtil.buildRegexString("s(i)?t*\\%", '\\')).isEqualTo("^s\\(i\\)\\?t\\*%$");
        assertThat(LikeUtil.buildRegexString("am\\e\\\\t", '\\')).isEqualTo("^ame\\\\t$");
        assertThat(LikeUtil.buildRegexString("x%%y%z%\\?", '%')).isEqualTo("^x%yz\\\\\\?$");
        assertThat(LikeUtil.buildRegexString("a__b_%_?c", '_')).isEqualTo("^a_b%\\?c$");
        assertThat(LikeUtil.buildRegexString("beegene?", 'e')).isEqualTo("^begn\\?$");
    }

}
