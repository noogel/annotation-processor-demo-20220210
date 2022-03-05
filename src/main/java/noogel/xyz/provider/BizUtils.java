package noogel.xyz.provider;

import java.nio.Buffer;


@StrengthenBuilder
public class BizUtils {
    @Strengthen(value = BizBufferProvider.class, param = Buffer.class)
    public Integer getResp1(String biz) {
        return 1;
    }

    @Strengthen(value = BizBufferProvider.class, param = Buffer.class)
    public String getResp2(String biz) {
        return "1";
    }

    @Strengthen(value = BizBufferProvider.class, param = Buffer.class)
    public Double getResp3(String biz) {
        return 1.0D;
    }

    @Strengthen(value = BizBufferProvider.class, param = Buffer.class)
    public Float getResp4(String biz) {
        return 1.0F;
    }

    @Strengthen(value = BizBufferProvider.class, param = Buffer.class)
    public Long getResp5(String biz) {
        return 1L;
    }
}
