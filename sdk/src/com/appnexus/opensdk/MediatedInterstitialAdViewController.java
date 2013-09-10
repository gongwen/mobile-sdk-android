package com.appnexus.opensdk;

import android.app.Activity;
import android.view.View;
import com.appnexus.opensdk.utils.Clog;

public class MediatedInterstitialAdViewController extends MediatedAdViewController implements Displayable {

    static public MediatedInterstitialAdViewController create(InterstitialAdView owner, AdResponse response) {
        MediatedInterstitialAdViewController out;
        try {
            out = new MediatedInterstitialAdViewController(owner, response);
        } catch (Exception e) {
            return null;
        }

        return out;

    }

    protected MediatedInterstitialAdViewController(InterstitialAdView owner, AdResponse response) throws Exception {
        super(owner, response);

        if (this.mAV == null || !(this.mAV instanceof MediatedInterstitialAdView)) {
            throw new Exception("Mediated view is null or not an instance of MediatedInterstitialAdView");
        }
    }


    protected void show() {
        if (mAV != null) {
            ((MediatedInterstitialAdView) mAV).show();
        }
    }

    //TODO: how come this is inconsistent with Banner controller? in banner controller we requestAd in the constructor, but for IADs we do that in getView
    //TODO: also we return null here; how to test if an interstitial returns an ad then?
    @Override
    public View getView() {
        Clog.d(Clog.mediationLogTag, Clog.getString(R.string.mediated_request));
        if (mAV == null) {
            Clog.e(Clog.mediationLogTag, "getView: MediatedAdView was null");
            return null;
        }
        if (owner == null) {
            Clog.e(Clog.mediationLogTag, "getView: owner was null");
            return null;
        }

        //TODO: refactor - this also depends on owner. what if owner is null? (for testing)
        try {
            ((MediatedInterstitialAdView) mAV).requestAd(this, (Activity) owner.getContext(), param, uid);
        } catch (Exception e) {
            Clog.e(Clog.mediationLogTag, "Exception in requestAd from mediated view", e);
            onAdFailed(RESULT.MEDIATED_SDK_UNAVAILABLE);
        } catch (Error e) {
            // catch errors. exceptions will be caught above.
            Clog.e(Clog.mediationLogTag, "Error in requestAd from mediated view", e);
            onAdFailed(RESULT.MEDIATED_SDK_UNAVAILABLE);
        }

        return null;
    }

}
