package com.ning.atlas.space;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import org.apache.commons.lang3.tuple.Pair;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SQLiteBackedSpace extends BaseSpace
{
    private static final Logger log = Logger.get(SQLiteBackedSpace.class);

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException e) {
            log.warn(e, "sqlite not available");
        }
    }


    private final Dao dao;

    private SQLiteBackedSpace(File dbFile) throws IOException
    {
        Files.createParentDirs(dbFile);
        log.debug("storing data in %s", dbFile.getAbsolutePath());

        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        this.dao = new DBI(url).onDemand(Dao.class);

        dao.create();
    }

    public static Space create(File storage) throws IOException
    {
        return new SQLiteBackedSpace(storage);
    }


    @Override
    protected String read(Identity id, String key) throws IOException
    {
        return dao.read(id.toExternalForm(), key);
    }

    @Override
    protected void write(Identity id, String key, String value) throws IOException
    {
        dao.write(id.toExternalForm(), key, value);
    }

    @Override
    protected Map<String, String> readAll(Identity prefix) throws IOException
    {
        List<Pair<String, String>> pairs = dao.readAll(prefix.toExternalForm());
        Map<String, String> rs = Maps.newHashMap();
        for (Pair<String, String> pair : pairs) {
            rs.put(pair.getKey(), pair.getValue());
        }
        return rs;
    }

    public static interface Dao
    {
        @SqlUpdate("create table if not exists space ( id varchar, key varchar, value varchar, primary key (id, key))")
        public void create();

        @SqlUpdate("insert into space (id, key, value) values (:id, :key, :value)")
        void write(@Bind("id") String id, @Bind("key") String key, @Bind("value") String value);

        @SqlQuery("select value from space where id = :id and key = :key")
        String read(@Bind("id") String id, @Bind("key") String key);

        @SqlQuery("select key, value from space where id = :id")
        @Mapper(MyMapper.class)
        List<Pair<String, String>> readAll(@Bind("id") String id);
    }

    public static class MyMapper implements ResultSetMapper<Pair<String, String>>
    {
        @Override
        public Pair<String, String> map(int index, ResultSet r, StatementContext ctx) throws SQLException
        {
            return Pair.of(r.getString("key"), r.getString("value"));
        }
    }
}
