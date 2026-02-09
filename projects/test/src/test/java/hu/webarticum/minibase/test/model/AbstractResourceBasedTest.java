package hu.webarticum.minibase.test.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

public abstract class AbstractResourceBasedTest {

    protected InputStream openResourceInputStream(String resourcePath, String resourceDescription) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertThat(in).as(resourceDescription).isNotNull();
        return in;
    }

}
