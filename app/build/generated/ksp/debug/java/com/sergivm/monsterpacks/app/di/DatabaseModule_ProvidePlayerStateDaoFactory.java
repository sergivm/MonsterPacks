package com.sergivm.monsterpacks.app.di;

import com.sergivm.monsterpacks.data.db.MonsterPacksDatabase;
import com.sergivm.monsterpacks.data.db.dao.PlayerStateDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class DatabaseModule_ProvidePlayerStateDaoFactory implements Factory<PlayerStateDao> {
  private final Provider<MonsterPacksDatabase> dbProvider;

  public DatabaseModule_ProvidePlayerStateDaoFactory(Provider<MonsterPacksDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlayerStateDao get() {
    return providePlayerStateDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePlayerStateDaoFactory create(
      Provider<MonsterPacksDatabase> dbProvider) {
    return new DatabaseModule_ProvidePlayerStateDaoFactory(dbProvider);
  }

  public static PlayerStateDao providePlayerStateDao(MonsterPacksDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePlayerStateDao(db));
  }
}
