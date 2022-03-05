package noogel.xyz.provider;

import java.nio.Buffer;
import java.util.function.Function;

public interface Provider {

    <U> U getResp(Buffer buffer, Function<String, U> function);
}
