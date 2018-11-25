package sql;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.jdbc.*;
import sql.model.SQLModel;
import sql.util.JsonSqlConfigParser;
import sql.util.SQLModelBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Tester {

	private static final Gson gson = new Gson();
	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
	private static final String EXGFX = "exgfx";
	private static final String TODO = "todo";
	private static final String TODO_ITEM = "todoitem";
	private static final String CONFIG_FILE = "./config/sqlconfig.json";
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		JsonSqlConfigParser sqlparser = new JsonSqlConfigParser();
		sqlparser.parseConfig(CONFIG_FILE);
		String modelDirectory = sqlparser.getModelDirectory();

		SQLModelBuilder builder = new SQLModelBuilder();
		builder.findModelFiles(modelDirectory);
		builder.readFiles();

		if(!builder.areModelsFormattedCorrectly()) {
			return;
		}

		Map<String, SQLModel> models = builder.getCopyOfModels();

		String dbName = sqlparser.getDbName();
		JDBCHandler pghandler = new PostgresHandler(models);
		JDBCEnum.addJDBCHandler(dbName, pghandler);

		JDBCHandler handler = JDBCEnum.getJDBCHandler(dbName);
		try {
			handler.setConnection(sqlparser.getDb(), sqlparser.getHost(), sqlparser.getPort(),
					sqlparser.getUsername(), sqlparser.getPassword());
			handler.createTables();
			LOGGER.debug(handler.getTable(TODO));
			LOGGER.debug(handler.getTable(TODO_ITEM));
			LOGGER.debug(handler.getTable(EXGFX));
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}

		// Executes update on exgfx insert
//		ColumnObject[] columns = new ColumnObject[5];
//		columns[0] = new ColumnObject<String>("filename", "ExGFX100", STRING);
//		columns[1] = new ColumnObject<String>("description", "Test graphics file.", STRING);
//		columns[2] = new ColumnObject<String>("type", "test", STRING);
//		columns[3] = new ColumnObject<Boolean>("completed", false, BOOLEAN);
//		columns[4] = new ColumnObject<String>("imglink", "www.google.com", STRING);
//
//		handler.executeUpdate(handler.insert("exgfx", columns));


//		ColumnObject[] todoRow1 = new ColumnObject[2];
//		todoRow1[0] = new ColumnObject<>("title", "First", STRING);
//		todoRow1[1] = new ColumnObject<>("owner", "Drew", STRING);
////		// insert todolists into table
////		// inserts and updated need executeUpdate as no data is returned
//		handler.executeUpdate(handler.insert("todo", todoRow1));
//
//
//		ColumnObject[] row1 = new ColumnObject[2];
//		row1[0] = new ColumnObject<>("content", "Finish commands.", STRING);
//		row1[1] = new ColumnObject<>("todoid", 1, INTEGER);
//
//
//		ColumnObject[] row2 = new ColumnObject[2];
//		row2[0] = new ColumnObject<>("content", "Test item.", STRING);
//		row2[1] = new ColumnObject<>("todoid", 1, INTEGER);
//
////		// insert into first todolist
//		handler.executeUpdate(handler.insert(TODO_ITEM, row1));
//		handler.executeUpdate(handler.insert(TODO_ITEM, row2));
//				STRING, "todoid", 1, INTEGER, "completed", false, BOOLEAN));
//
//		// insert into second todolist
//		handler.executeUpdate(handler.insert("todoitem", "content", "Finish JDBCHandler.",
//				STRING, "todoid", 2, INTEGER, "completed", false, BOOLEAN));



		//SELECT event.description FROM
		//((user INNER JOIN tickets ON (user.userId=tickets.userId AND user.name="Bob"))
		//INNER JOIN event ON ticket.eventId=event.eventId);
// Useful for joining results from multiple tables in the same db
/*
		ResultSet results = handler.executeQuery(handler.select("content",
				handler.from(
						handler.openParentheses(
								handler.openParentheses(
										handler.innerJoin(TODO, TODO_ITEM,
												handler.on(
														handler.openParentheses("todo.todoid"), "todoitem.todoid", INTEGER,
														handler.and("owner", "Drew", STRING,
																handler.closeParentheses(handler.closeParentheses(
																		handler.closeParentheses("")
																))))
										)
								)
						)
						, "")
		));

		String out = ResultSetHandler.getResults(results);
		System.out.println(out);
*/

		// Called in one service; when user POSTs, service will send GET to other service to receive json results
//		String referenceKey = "todoid";
//		int referenceId = 1;
//		String referenceInfo = ResultSetHandler.getInfoFromReference(handler, TODO, referenceKey, referenceId);
//
//		// This is the result that will be received by other service
//		System.out.println(referenceInfo);
////		JsonObject obj = ResultSetHandler.getInfoFromJson(referenceInfo);
//////		String referencePrimaryKey = obj.get("primaryKey").getAsString();
//////		int id = obj.get("id").getAsInt();
//		String referencePrimaryKey = ResultSetHandler.getTableNameFromJson(referenceInfo);
//		int id = ResultSetHandler.getTableIdFromJson(referenceInfo);
//
//		System.out.println(ResultSetHandler.getResultSetWithReference(handler, TODO_ITEM, referencePrimaryKey, id));
//		System.out.println(ResultSetHandler.getResultSetWithReference(handler, TODO_ITEM, referencePrimaryKey, 2));
	}
}
