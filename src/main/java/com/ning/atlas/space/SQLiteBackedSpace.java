package com.ning.atlas.space;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.space.SpaceKey;
import org.apache.commons.lang3.tuple.Pair;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.logging.PrintStreamLog;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.StringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        DBI dbi = new DBI(url);
        this.dao = dbi.onDemand(Dao.class);

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
    protected Map<SpaceKey, String> readAll(Identity prefix) throws IOException
    {
        List<List<String>> pairs = dao.readAll(prefix.toExternalForm(), prefix.toExternalForm() + "/%");
        Map<SpaceKey, String> rs = Maps.newHashMap();
        for (List<String> pair : pairs) {
            Identity id = Identity.valueOf(pair.get(0));
            SpaceKey key = SpaceKey.from(id, pair.get(1));
            rs.put(key, pair.get(2));
        }
        return rs;
    }


    @Override
    public Set<Identity> findAllIdentities()
    {
        return Sets.newHashSet(dao.findAllIds());
    }

    @Override
    public void deleteAll(Identity identity)
    {
        dao.deleteAllWithId(identity.toExternalForm(), identity.toExternalForm() + "/%");
    }

    @Override
    public void delete(Identity identity, String key)
    {
        dao.delete(identity.toExternalForm(), key);
    }

    public static interface Dao
    {
        @SqlUpdate("create table if not exists space ( id varchar, key varchar, value varchar, primary key (id, key))")
        public void create();

        @SqlUpdate("insert or replace into space (id, key, value) values (:id, :key, :value)")
        void write(@Bind("id") String id, @Bind("key") String key, @Bind("value") String value);

        @SqlQuery("select value from space where id = :id and key = :key")
        String read(@Bind("id") String id, @Bind("key") String key);

        @SqlQuery("select id, key, value from space where (id = :id) or (id like :id_pattern)")
        @Mapper(MyMapper.class)
        List<List<String>> readAll(@Bind("id") String id, @Bind("id_pattern") String idPattern);

        @SqlQuery("select distinct id from space")
        @Mapper(MyIdMapper.class)
        List<Identity> findAllIds();

        @SqlUpdate("delete from space where id = :id or id like :id_pattern")
        void deleteAllWithId(@Bind("id") String id, @Bind("id_pattern") String pattern);

        @SqlUpdate("delete from space where id = :id and key = :key")
        void delete(@Bind("id") String id, @Bind("key") String key);
    }

    public static class MyIdMapper implements ResultSetMapper<Identity>
    {

        @Override
        public Identity map(int index, ResultSet r, StatementContext ctx) throws SQLException
        {
            return Identity.valueOf(r.getString("id"));
        }
    }

    public static class MyMapper implements ResultSetMapper<List<String>>
    {
        @Override
        public List<String> map(int index, ResultSet r, StatementContext ctx) throws SQLException
        {
            return ImmutableList.of(r.getString("id"), r.getString("key"), r.getString("value"));
        }
    }
}
