package com.monke.monkeybook.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.monke.monkeybook.bean.ChapterBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CHAPTER_BEAN".
*/
public class ChapterBeanDao extends AbstractDao<ChapterBean, String> {

    public static final String TABLENAME = "CHAPTER_BEAN";

    /**
     * Properties of entity ChapterBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property NoteUrl = new Property(0, String.class, "noteUrl", false, "NOTE_URL");
        public final static Property DurChapterIndex = new Property(1, Integer.class, "durChapterIndex", false, "DUR_CHAPTER_INDEX");
        public final static Property DurChapterUrl = new Property(2, String.class, "durChapterUrl", true, "DUR_CHAPTER_URL");
        public final static Property DurChapterName = new Property(3, String.class, "durChapterName", false, "DUR_CHAPTER_NAME");
        public final static Property DurChapterPlayUrl = new Property(4, String.class, "durChapterPlayUrl", false, "DUR_CHAPTER_PLAY_URL");
        public final static Property Tag = new Property(5, String.class, "tag", false, "TAG");
        public final static Property Start = new Property(6, Integer.class, "start", false, "START");
        public final static Property End = new Property(7, Integer.class, "end", false, "END");
    }


    public ChapterBeanDao(DaoConfig config) {
        super(config);
    }
    
    public ChapterBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CHAPTER_BEAN\" (" + //
                "\"NOTE_URL\" TEXT," + // 0: noteUrl
                "\"DUR_CHAPTER_INDEX\" INTEGER," + // 1: durChapterIndex
                "\"DUR_CHAPTER_URL\" TEXT PRIMARY KEY NOT NULL ," + // 2: durChapterUrl
                "\"DUR_CHAPTER_NAME\" TEXT," + // 3: durChapterName
                "\"DUR_CHAPTER_PLAY_URL\" TEXT," + // 4: durChapterPlayUrl
                "\"TAG\" TEXT," + // 5: tag
                "\"START\" INTEGER," + // 6: start
                "\"END\" INTEGER);"); // 7: end
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CHAPTER_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ChapterBean entity) {
        stmt.clearBindings();
 
        String noteUrl = entity.getNoteUrl();
        if (noteUrl != null) {
            stmt.bindString(1, noteUrl);
        }
 
        Integer durChapterIndex = entity.getDurChapterIndex();
        if (durChapterIndex != null) {
            stmt.bindLong(2, durChapterIndex);
        }
 
        String durChapterUrl = entity.getDurChapterUrl();
        if (durChapterUrl != null) {
            stmt.bindString(3, durChapterUrl);
        }
 
        String durChapterName = entity.getDurChapterName();
        if (durChapterName != null) {
            stmt.bindString(4, durChapterName);
        }
 
        String durChapterPlayUrl = entity.getDurChapterPlayUrl();
        if (durChapterPlayUrl != null) {
            stmt.bindString(5, durChapterPlayUrl);
        }
 
        String tag = entity.getTag();
        if (tag != null) {
            stmt.bindString(6, tag);
        }
 
        Integer start = entity.getStart();
        if (start != null) {
            stmt.bindLong(7, start);
        }
 
        Integer end = entity.getEnd();
        if (end != null) {
            stmt.bindLong(8, end);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ChapterBean entity) {
        stmt.clearBindings();
 
        String noteUrl = entity.getNoteUrl();
        if (noteUrl != null) {
            stmt.bindString(1, noteUrl);
        }
 
        Integer durChapterIndex = entity.getDurChapterIndex();
        if (durChapterIndex != null) {
            stmt.bindLong(2, durChapterIndex);
        }
 
        String durChapterUrl = entity.getDurChapterUrl();
        if (durChapterUrl != null) {
            stmt.bindString(3, durChapterUrl);
        }
 
        String durChapterName = entity.getDurChapterName();
        if (durChapterName != null) {
            stmt.bindString(4, durChapterName);
        }
 
        String durChapterPlayUrl = entity.getDurChapterPlayUrl();
        if (durChapterPlayUrl != null) {
            stmt.bindString(5, durChapterPlayUrl);
        }
 
        String tag = entity.getTag();
        if (tag != null) {
            stmt.bindString(6, tag);
        }
 
        Integer start = entity.getStart();
        if (start != null) {
            stmt.bindLong(7, start);
        }
 
        Integer end = entity.getEnd();
        if (end != null) {
            stmt.bindLong(8, end);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2);
    }    

    @Override
    public ChapterBean readEntity(Cursor cursor, int offset) {
        ChapterBean entity = new ChapterBean( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // noteUrl
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1), // durChapterIndex
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // durChapterUrl
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // durChapterName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // durChapterPlayUrl
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // tag
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6), // start
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7) // end
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ChapterBean entity, int offset) {
        entity.setNoteUrl(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setDurChapterIndex(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
        entity.setDurChapterUrl(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setDurChapterName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setDurChapterPlayUrl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setTag(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setStart(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
        entity.setEnd(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
     }
    
    @Override
    protected final String updateKeyAfterInsert(ChapterBean entity, long rowId) {
        return entity.getDurChapterUrl();
    }
    
    @Override
    public String getKey(ChapterBean entity) {
        if(entity != null) {
            return entity.getDurChapterUrl();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ChapterBean entity) {
        return entity.getDurChapterUrl() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
