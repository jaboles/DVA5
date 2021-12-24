package jb.dvacommon.ui;

import java.net.URL;
import java.util.List;

public interface DVATextVerifyListener
{
    void OnVerified(List<URL> verifiedSoundUrls);
    void OnFailed();
}
