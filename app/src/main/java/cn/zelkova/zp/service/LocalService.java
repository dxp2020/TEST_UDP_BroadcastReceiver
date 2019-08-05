package cn.zelkova.zp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import cn.zelkova.zp.IServiceBinder;

public class LocalService extends Service {

    private int serviceStatus = 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder(serviceStatus);
    }

    private class LocalBinder extends IServiceBinder.Stub{

        private int serviceStatus;

        public LocalBinder(int serviceStatus) {
            this.serviceStatus = serviceStatus;
        }

        @Override
        public int getServiceStatus() throws RemoteException {
            return serviceStatus;
        }
    }
}
