package org.javalite.activejdbc.dialects;

import java.util.List;



public class PostgreSQLDialect extends DefaultDialect {

    /**
     * Generates adds limit, offset and order bys to a sub-query
     *
     * @param tableName name of table. If table name is null, then the subQuery parameter is considered to be a full query, and all that needs to be done is to
     * add limit, offset and order bys
     * @param subQuery sub-query or a full query
     * @param orderBys
     * @param limit
     * @param offset
     * @return query with
     */
    @Override
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset, String... columns) {
        StringBuilder fullQuery = new StringBuilder();
        
        appendSelect(fullQuery, tableName, null, subQuery, orderBys, columns);

        if(limit != -1){
            fullQuery.append(" LIMIT ").append(limit);
        }

        if(offset != -1){
            fullQuery.append(" OFFSET ").append(offset);
        }

        return fullQuery.toString();
    }
}
