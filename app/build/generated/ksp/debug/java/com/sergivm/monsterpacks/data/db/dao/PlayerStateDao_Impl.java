package com.sergivm.monsterpacks.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.sergivm.monsterpacks.data.db.entity.PlayerStateEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlayerStateDao_Impl implements PlayerStateDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayerStateEntity> __insertionAdapterOfPlayerStateEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public PlayerStateDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayerStateEntity = new EntityInsertionAdapter<PlayerStateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `player_state` (`id`,`username`,`usernameChanged`,`coins`,`gems`,`xp`,`level`,`cardCopiesJson`,`bonusPackReadyAtMs`,`basicPackUpgradeLevel`,`bonusPackUpgradeLevel`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayerStateEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUsername());
        final int _tmp = entity.getUsernameChanged() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getCoins());
        statement.bindLong(5, entity.getGems());
        statement.bindLong(6, entity.getXp());
        statement.bindLong(7, entity.getLevel());
        statement.bindString(8, entity.getCardCopiesJson());
        if (entity.getBonusPackReadyAtMs() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getBonusPackReadyAtMs());
        }
        statement.bindLong(10, entity.getBasicPackUpgradeLevel());
        statement.bindLong(11, entity.getBonusPackUpgradeLevel());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM player_state";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final PlayerStateEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlayerStateEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<PlayerStateEntity> observe() {
    final String _sql = "SELECT * FROM player_state WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"player_state"}, new Callable<PlayerStateEntity>() {
      @Override
      @Nullable
      public PlayerStateEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfUsernameChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "usernameChanged");
          final int _cursorIndexOfCoins = CursorUtil.getColumnIndexOrThrow(_cursor, "coins");
          final int _cursorIndexOfGems = CursorUtil.getColumnIndexOrThrow(_cursor, "gems");
          final int _cursorIndexOfXp = CursorUtil.getColumnIndexOrThrow(_cursor, "xp");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfCardCopiesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "cardCopiesJson");
          final int _cursorIndexOfBonusPackReadyAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "bonusPackReadyAtMs");
          final int _cursorIndexOfBasicPackUpgradeLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "basicPackUpgradeLevel");
          final int _cursorIndexOfBonusPackUpgradeLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "bonusPackUpgradeLevel");
          final PlayerStateEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final boolean _tmpUsernameChanged;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfUsernameChanged);
            _tmpUsernameChanged = _tmp != 0;
            final long _tmpCoins;
            _tmpCoins = _cursor.getLong(_cursorIndexOfCoins);
            final long _tmpGems;
            _tmpGems = _cursor.getLong(_cursorIndexOfGems);
            final long _tmpXp;
            _tmpXp = _cursor.getLong(_cursorIndexOfXp);
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final String _tmpCardCopiesJson;
            _tmpCardCopiesJson = _cursor.getString(_cursorIndexOfCardCopiesJson);
            final Long _tmpBonusPackReadyAtMs;
            if (_cursor.isNull(_cursorIndexOfBonusPackReadyAtMs)) {
              _tmpBonusPackReadyAtMs = null;
            } else {
              _tmpBonusPackReadyAtMs = _cursor.getLong(_cursorIndexOfBonusPackReadyAtMs);
            }
            final int _tmpBasicPackUpgradeLevel;
            _tmpBasicPackUpgradeLevel = _cursor.getInt(_cursorIndexOfBasicPackUpgradeLevel);
            final int _tmpBonusPackUpgradeLevel;
            _tmpBonusPackUpgradeLevel = _cursor.getInt(_cursorIndexOfBonusPackUpgradeLevel);
            _result = new PlayerStateEntity(_tmpId,_tmpUsername,_tmpUsernameChanged,_tmpCoins,_tmpGems,_tmpXp,_tmpLevel,_tmpCardCopiesJson,_tmpBonusPackReadyAtMs,_tmpBasicPackUpgradeLevel,_tmpBonusPackUpgradeLevel);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object get(final Continuation<? super PlayerStateEntity> $completion) {
    final String _sql = "SELECT * FROM player_state WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlayerStateEntity>() {
      @Override
      @Nullable
      public PlayerStateEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfUsernameChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "usernameChanged");
          final int _cursorIndexOfCoins = CursorUtil.getColumnIndexOrThrow(_cursor, "coins");
          final int _cursorIndexOfGems = CursorUtil.getColumnIndexOrThrow(_cursor, "gems");
          final int _cursorIndexOfXp = CursorUtil.getColumnIndexOrThrow(_cursor, "xp");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfCardCopiesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "cardCopiesJson");
          final int _cursorIndexOfBonusPackReadyAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "bonusPackReadyAtMs");
          final int _cursorIndexOfBasicPackUpgradeLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "basicPackUpgradeLevel");
          final int _cursorIndexOfBonusPackUpgradeLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "bonusPackUpgradeLevel");
          final PlayerStateEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final boolean _tmpUsernameChanged;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfUsernameChanged);
            _tmpUsernameChanged = _tmp != 0;
            final long _tmpCoins;
            _tmpCoins = _cursor.getLong(_cursorIndexOfCoins);
            final long _tmpGems;
            _tmpGems = _cursor.getLong(_cursorIndexOfGems);
            final long _tmpXp;
            _tmpXp = _cursor.getLong(_cursorIndexOfXp);
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final String _tmpCardCopiesJson;
            _tmpCardCopiesJson = _cursor.getString(_cursorIndexOfCardCopiesJson);
            final Long _tmpBonusPackReadyAtMs;
            if (_cursor.isNull(_cursorIndexOfBonusPackReadyAtMs)) {
              _tmpBonusPackReadyAtMs = null;
            } else {
              _tmpBonusPackReadyAtMs = _cursor.getLong(_cursorIndexOfBonusPackReadyAtMs);
            }
            final int _tmpBasicPackUpgradeLevel;
            _tmpBasicPackUpgradeLevel = _cursor.getInt(_cursorIndexOfBasicPackUpgradeLevel);
            final int _tmpBonusPackUpgradeLevel;
            _tmpBonusPackUpgradeLevel = _cursor.getInt(_cursorIndexOfBonusPackUpgradeLevel);
            _result = new PlayerStateEntity(_tmpId,_tmpUsername,_tmpUsernameChanged,_tmpCoins,_tmpGems,_tmpXp,_tmpLevel,_tmpCardCopiesJson,_tmpBonusPackReadyAtMs,_tmpBasicPackUpgradeLevel,_tmpBonusPackUpgradeLevel);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
