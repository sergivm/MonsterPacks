package com.sergivm.monsterpacks.data.repository;

import com.google.gson.Gson;
import com.sergivm.monsterpacks.data.db.dao.PlayerStateDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class GameRepositoryImpl_Factory implements Factory<GameRepositoryImpl> {
  private final Provider<PlayerStateDao> daoProvider;

  private final Provider<Gson> gsonProvider;

  public GameRepositoryImpl_Factory(Provider<PlayerStateDao> daoProvider,
      Provider<Gson> gsonProvider) {
    this.daoProvider = daoProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public GameRepositoryImpl get() {
    return newInstance(daoProvider.get(), gsonProvider.get());
  }

  public static GameRepositoryImpl_Factory create(Provider<PlayerStateDao> daoProvider,
      Provider<Gson> gsonProvider) {
    return new GameRepositoryImpl_Factory(daoProvider, gsonProvider);
  }

  public static GameRepositoryImpl newInstance(PlayerStateDao dao, Gson gson) {
    return new GameRepositoryImpl(dao, gson);
  }
}
