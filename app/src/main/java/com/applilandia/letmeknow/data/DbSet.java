package com.applilandia.letmeknow.data;

import android.content.Context;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public abstract class DbSet<Entity> {

    protected Context mContext;
    protected UnitOfWork mUnitOfWork;

    public DbSet(Context context) {
        mContext = context;
        mUnitOfWork = new UnitOfWork(context);
    }

    public DbSet(Context context, UnitOfWork unitOfWork) {
        mContext = context;
        mUnitOfWork = unitOfWork;
    }

    public void initWork() {
        mUnitOfWork.init();
    }

    public void endWork(boolean successful) {
        if (successful) {
            mUnitOfWork.commit();
        } else {
            mUnitOfWork.rollback();
        }
    }

    public UnitOfWork getUnitOfWork() {
        return mUnitOfWork;
    }

    public abstract long create(Entity entity) throws Exception;

    public abstract int update(Entity entity) throws Exception;

    public abstract boolean delete(Entity entity);

    public abstract boolean find(int id);
}
