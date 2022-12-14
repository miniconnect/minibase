package hu.webarticum.minibase.query.query;

public interface Queries {

    public static SelectQuery.SelectQueryBuilder select() {
        return SelectQuery.builder();
    }

    public static SelectCountQuery.SelectCountQueryBuilder selectCount() {
        return SelectCountQuery.builder();
    }

    public static StandaloneSelectQuery.StandaloneSelectQueryBuilder standaloneSelect() {
        return StandaloneSelectQuery.builder();
    }

    public static ShowSpecialQuery.ShowSpecialQueryBuilder showSpecial() {
        return ShowSpecialQuery.builder();
    }

    public static InsertQuery.InsertQueryBuilder insert() {
        return InsertQuery.builder();
    }

    public static UpdateQuery.UpdateQueryBuilder update() {
        return UpdateQuery.builder();
    }

    public static DeleteQuery.DeleteQueryBuilder delete() {
        return DeleteQuery.builder();
    }

    public static ShowSchemasQuery.ShowSchemasQueryBuilder showSchemas() {
        return ShowSchemasQuery.builder();
    }

    public static ShowTablesQuery.ShowTablesQueryBuilder showTables() {
        return ShowTablesQuery.builder();
    }

    public static UseQuery.UseQueryBuilder use() {
        return UseQuery.builder();
    }

    public static SetVariableQuery.SetVariableQueryBuilder setVariable() {
        return SetVariableQuery.builder();
    }

}
