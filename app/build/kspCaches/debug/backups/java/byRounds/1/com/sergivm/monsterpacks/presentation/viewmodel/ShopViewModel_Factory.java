package com.sergivm.monsterpacks.presentation.viewmodel;

import com.sergivm.monsterpacks.data.repository.GameRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ShopViewModel_Factory implements Factory<ShopViewModel> {
  private final Provider<GameRepository> repositoryProvider;

  public ShopViewModel_Factory(Provider<GameRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ShopViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ShopViewModel_Factory create(Provider<GameRepository> repositoryProvider) {
    return new ShopViewModel_Factory(repositoryProvider);
  }

  public static ShopViewModel newInstance(GameRepository repository) {
    return new ShopViewModel(repository);
  }
}
