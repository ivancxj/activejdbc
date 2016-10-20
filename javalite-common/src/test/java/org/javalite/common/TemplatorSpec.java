package org.javalite.common;

import org.junit.Test;

/**
 * @author igor on 9/30/16.
 */
public class TemplatorSpec {

    @Test
    public void shouldMergeTemplateWithTemplatorInstance(){
        Templator t = new Templator("/templator/hello.txt");
//        a(t.merge(map("name", "Igor", "greeting", "Hi"))).shouldBeEqual("Hi, Igor");
    }

    @Test
    public void shouldMergeTemplateWithStaticTemplator(){
//        a(Templator.mergeFromPath("/templator/hello.txt", map("name", "Igor", "greeting", "Hi"))).shouldBeEqual("Hi, Igor");
    }
}


