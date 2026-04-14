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
public final class CollectionViewModel_Factory implements Factory<CollectionViewModel> {
  private final Provider<GameRepository> repositoryProvider;

  public CollectionViewModel_Factory(Provider<GameRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CollectionViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static CollectionViewModel_Factory create(Provider<GameRepository> repositoryProvider) {
    return new CollectionViewModel_Factory(repositoryProvider);
  }

  public static CollectionViewModel newInstance(GameRepository repository) {
    return new CollectionViewModel(repository);
  }
}
