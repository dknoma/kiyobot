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
			LOGGER.debug(handler.getTable("client"));
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}

		// Executes update on exgfx insert
//		ColumnObject[] columns = new ColumnObject[5];
//		columns[0] = new ColumnObject<String>("filename", "ExGFX100");
//		columns[1] = new ColumnObject<String>("description", "Test graphics file.");
//		columns[2] = new ColumnObject<String>("type", "test");
//		columns[3] = new ColumnObject<Boolean>("completed", false);
//		columns[4] = new ColumnObject<String>("imglink", "www.google.com");
//
//		handler.executeUpdate(handler.insert("exgfx", columns));


//		ColumnObject[] todoRow1 = new ColumnObject[2];
//		todoRow1[0] = new ColumnObject<>("title", "First");
//		todoRow1[1] = new ColumnObject<>("todoer", "Drew");
////		// insert todolists into table
////		// inserts and updated need executeUpdate as no data is returned
//		handler.executeUpdate(handler.insert(TODO, todoRow1));
//
//
//		todoRow1[0] = new ColumnObject<>("title", "Second");
//		todoRow1[1] = new ColumnObject<>("todoer", "Bobe");
////		// insert todolists into table
////		// inserts and updated need executeUpdate as no data is returned
//		handler.executeUpdate(handler.insert(TODO, todoRow1));
//
//		todoRow1[0] = new ColumnObject<>("title", "WOW");
//		todoRow1[1] = new ColumnObject<>("todoer", "Drew");
////		// insert todolists into table
////		// inserts and updated need executeUpdate as no data is returned
//		handler.executeUpdate(handler.insert(TODO, todoRow1));
//
//		todoRow1[0] = new ColumnObject<>("title", "WEHE");
//		todoRow1[1] = new ColumnObject<>("todoer", "Bob");
////		// insert todolists into table
////		// inserts and updated need executeUpdate as no data is returned
//		handler.executeUpdate(handler.insert(TODO, todoRow1));
//
//
//		todoRow1[0] = new ColumnObject<>("title", "Ehhh");
//		todoRow1[1] = new ColumnObject<>("todoer", "Drew");
////		// insert todolists into table
////		// inserts and updated need executeUpdate as no data is returned
//		handler.executeUpdate(handler.insert(TODO, todoRow1));
//
//
//
//
//
////
////		ColumnObject[] titleFirst = new ColumnObject[2];
////		titleFirst[0] = new ColumnObject<>("title", "Second");
////		titleFirst[1] = new ColumnObject<>("owner", "Bob");
////		handler.executeUpdate(handler.insert("todo", titleFirst));
////
//		ColumnObject[] row1 = new ColumnObject[2];
//		row1[0] = new ColumnObject<>("content", "Finish commands.");
//		row1[1] = new ColumnObject<>("todoid", 1);
//		handler.executeUpdate(handler.insert(TODO_ITEM, row1));
//
//
//		ColumnObject[] row2 = new ColumnObject[2];
//		row2[0] = new ColumnObject<>("content", "Spooooky.");
//		row2[1] = new ColumnObject<>("todoid", 1);
//		handler.executeUpdate(handler.insert(TODO_ITEM, row2));
////
//////		// insert into first todolist
//		row2[0] = new ColumnObject<>("content", "Blep.");
//		row2[1] = new ColumnObject<>("todoid", 3);
//		handler.executeUpdate(handler.insert(TODO_ITEM, row2));
//
//		row2[0] = new ColumnObject<>("content", "BBorf.");
//		row2[1] = new ColumnObject<>("todoid", 5);
//		handler.executeUpdate(handler.insert(TODO_ITEM, row2));
//
//		row2[0] = new ColumnObject<>("content", "REEEEE.");
//		row2[1] = new ColumnObject<>("todoid", 4);
//		handler.executeUpdate(handler.insert(TODO_ITEM, row2));

//		// insert into second todolist
//		handler.executeUpdate(handler.insert("todoitem", "content", "Finish JDBCHandler.",
//				STRING, "todoid", 2, INTEGER, "completed", false, BOOLEAN));



		//SELECT event.description FROM
		//((user INNER JOIN tickets ON (user.userId=tickets.userId AND user.name="Bob"))
		//INNER JOIN event ON ticket.eventId=event.eventId);


		/* Useful for joining results from multiple tables in the same db */
		ColumnObject[] ands = new ColumnObject[2];
		ands[0] = new ColumnObject<>("todo.todoid", "todoitem.todoid");
		ands[1] = new ColumnObject<>("todo.todoid", 1);
		ResultSet outerResults = ResultSetHandler.getResultSet(handler, "*", TODO, "todo.todoid", 1);
		ResultSet innerResults = selectItemFromInnerJoinOn(handler, "todoitem.*", TODO, TODO_ITEM, ands);

		// Does a comination of queries to find a specific item meeting the requirements of the and comparison queries
		// Then takes the result set and outputs json representing all the results and the table they are from
//		String outs = ResultSetHandler.allResultsToString(resultss);
//		System.out.println("asfasf: " + outs);

		System.out.println(String.format("Includes: %s", ResultSetHandler.getResultsIncluding(outerResults, innerResults)));

		ColumnObject[] titleFirst = new ColumnObject[2];
//		titleFirst[0] = new ColumnObject<>("title", "First");
		titleFirst[0] = new ColumnObject<>("todoer", "Drew");

//		ResultSet res = handler.executeQuery();
//		String s = ResultSetHandler.ge
		System.out.println(ResultSetHandler.findAll(handler, TODO, titleFirst));
		System.out.println(ResultSetHandler.findAll(handler, TODO));

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

	public static ResultSet selectItemFromInnerJoinOn(JDBCHandler handler, String select, String leftJoin, String rightJoin,
													  ColumnObject... comparisons) {
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i < comparisons.length; i++) {
			sb.append(handler.and(comparisons[i].getKey(), comparisons[i].getValue(), ""));
		}
		String andQueries = sb.toString();

		// SELECT *
		return handler.executeQuery(handler.select(select,
				// FROM ((
				handler.from(
						handler.openParentheses(
								handler.openParentheses(
										// * INNER JOIN *
										handler.innerJoin(leftJoin, rightJoin,
												// ON (*=* AND *=* AND ...)))
												handler.on(
														handler.openParentheses(comparisons[0].getKey()), comparisons[0].getValue(),
														andQueries +
														handler.closeParentheses(
																handler.closeParentheses(
																	handler.closeParentheses("")
																)
														)
												)
										)
								)
						)
						, "")
		));
		// SELECT * FROM ((* INNER JOIN * ON (*=* AND *=* AND...)))
	}
}
