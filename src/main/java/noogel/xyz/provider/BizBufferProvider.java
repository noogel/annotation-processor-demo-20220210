package noogel.xyz.provider;

import java.nio.Buffer;
import java.util.function.Function;

public class BizBufferProvider implements Provider {

    @Override
    public <U> U getResp(Buffer buffer, Function<String, U> function) {
        String biz = "1";
        return function.apply(biz);
    }
}
