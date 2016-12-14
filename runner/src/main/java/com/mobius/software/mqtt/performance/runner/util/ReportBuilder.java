package com.mobius.software.mqtt.performance.runner.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.performance.api.data.ErrorReport;
import com.mobius.software.mqtt.performance.api.data.SummaryData;
import com.mobius.software.mqtt.performance.commons.data.CommandType;
import com.mobius.software.mqtt.performance.commons.data.Counter;
import com.mobius.software.mqtt.performance.runner.data.TableRow;

public class ReportBuilder
{
	private static final int SCREEN_WIDTH = 102;
	private static final int ERROR_FILE_WIDTH = 140;
	private static final int COLUMNS = 3;
	private static final int COLUMN_WIDTH = SCREEN_WIDTH / COLUMNS;
	private static final int TIMESTAMP_LENGTH = 13;

	private static final String MINUS = "-";
	private static final String PLUS = "+";
	private static final String SPACE = " ";
	private static final String PIPE = "|";
	private static final String NEW_LINE = "\n";
	private static final String DOTS = ": ";

	private static final String ID = "  Scenario-ID:  ";
	private static final String RESULT_SUCCESS = " Result: SUCCESS ";
	private static final String RESULT_FAILED = " Result: FAILED ";
	private static final String START_TIME = " Start Time ";
	private static final String FINISH_TIME = " Finish Time ";
	private static final String CURRENT_TIME = " Current Time ";
	private static final String TOTAL_CLIENTS = " Total clients ";
	private static final String TOTAL_COMMANDS = " Total commands ";
	private static final String TOTAL_ERRORS = " Errors occured ";
	private static final String SUCCESSFULY_FINISHED = " Successfuly finished ";
	private static final String FAILED = " Failed ";
	private static final String DIDNT_FINISH = "didn't finish";
	private static final String DUPLICATES_IN = " Duplicates received ";
	private static final String DUPLICATES_OUT = " Duplicates sent ";
	private static final String OUTGOING_COUNTERS = " Outgoing counters ";
	private static final String INCOMING_COUNTERS = " Incoming counters ";
	private static final String COUNTER_NAME = " Counter Name ";
	private static final String COUNTER_VALUE = " Counter Value ";

	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	private static final String IDENTIFIER = " Client identifier ";
	private static final String TIME = "Time";
	private static final String MESSAGE = "Error message";

	public static String buildSummary(SummaryData data)
	{
		StringBuilder sb = new StringBuilder();

		String tableLine = drawTableLine();
		String header = drawSummaryHeader(data.getScenarioID(), data.getStatus());
		String timers = drawSummaryTimers(data.getStartTime(), data.getFinishTime());
		String totals = drawSummaryTotals(data.getTotalClients(), data.getTotalCommands(), data.getFinishedClients(), data.getFinishedCommands(), data.getFailedClients(), data.getFailedCommands(), data.getTotalErrors(), data.getDuplicatesIn(), data.getDuplicatesOut());
		String packetsTable = drawSummaryPacketsTable(data.getOutgoingCounters(), data.getIncomingCounters());
		sb.append(header);
		sb.append(timers);
		sb.append(tableLine);
		sb.append(totals);
		sb.append(packetsTable);
		sb.append(NEW_LINE);

		return sb.toString();
	}

	public static String buildError(String identifier, List<ErrorReport> clientErrors)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(drawErrorHeader(identifier));
		sb.append(drawErrorTableHeader());

		for (ErrorReport report : clientErrors)
		{
			String firstColumn = drawErrorTableColumn(arrangeTimeContent(report.getTimestamp()));
			String secondColumn = arrangeMessageContent(report.getMessage() + SPACE + report.getType().toString());
			sb.append(PIPE).append(firstColumn).append(PIPE).append(secondColumn).append(NEW_LINE);
		}
		sb.append(NEW_LINE);
		return sb.toString();
	}

	private static String arrangeMessageContent(String message)
	{
		StringBuilder sb = new StringBuilder();
		List<String> strings = new ArrayList<String>();
		int index = 0;
		while (index < message.length())
		{
			strings.add(message.substring(index, Math.min(index + ERROR_FILE_WIDTH / 2, message.length())));
			index += ERROR_FILE_WIDTH / 2;
		}

		for (int i = 0; i < strings.size(); i++)
		{
			if (i != 0)
			{
				sb.append(NEW_LINE).append(PIPE);
				String spaces = drawSpaces(ERROR_FILE_WIDTH / 2 - PIPE.length());
				sb.append(PIPE).append(spaces);
			}
			String contentSpaces = drawSpaces(ERROR_FILE_WIDTH / 2 - strings.get(i).length() - PIPE.length() - SPACE.length());
			sb.append(SPACE).append(strings.get(i)).append(contentSpaces).append(PIPE);
		}

		return sb.toString();
	}

	private static String drawErrorHeader(String value)
	{
		StringBuilder sb = new StringBuilder();
		String spaces = drawLine(4, 1, 0);
		int prefix = 0;
		int suffix = 0;
		int contentLength = IDENTIFIER.length() + value.length() + spaces.length() + prefix + suffix;
		String leftLine = drawLine((ERROR_FILE_WIDTH - contentLength) / 2, 0, suffix);
		String rightLine = drawLine((ERROR_FILE_WIDTH - contentLength) / 2, prefix, 0);
		sb.append(leftLine).append(IDENTIFIER).append(spaces);
		sb.append(value).append(rightLine);
		String header = trimContentLine(sb.toString(), MINUS, ERROR_FILE_WIDTH);
		return header + NEW_LINE;
	}

	private static String drawErrorTableHeader()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(PIPE).append(drawErrorTableHeaderColumn(TIME)).append(PIPE);
		sb.append(drawErrorTableHeaderColumn(MESSAGE)).append(PIPE).append(NEW_LINE);
		return sb.toString();
	}

	private static String drawErrorTableHeaderColumn(String value)
	{
		StringBuilder sb = new StringBuilder();
		int length = ERROR_FILE_WIDTH / 2;
		int spacesLength = (length - value.length()) / 2;
		String spaces = drawSpaces(spacesLength);
		sb.append(spaces).append(value).append(spaces);
		return sb.toString();
	}

	private static String drawErrorTableColumn(String value)
	{
		StringBuilder sb = new StringBuilder();
		int length = ERROR_FILE_WIDTH / 2;
		int spacesLength = length - value.length();
		String spaces = drawSpaces(spacesLength);
		sb.append(value).append(spaces);
		String column = sb.toString();
		return trimContentLine(column, SPACE, length);
	}

	private static String arrangeTimeContent(long timestamp)
	{
		StringBuilder sb = new StringBuilder();
		String dateTime = timestampToDateTime(timestamp);
		sb.append(SPACE).append(dateTime).append(SPACE).append(PIPE).append(SPACE).append(timestamp);
		return sb.toString();
	}

	private static String drawSummaryPacketsTable(Map<CommandType, AtomicInteger> outMap, Map<CommandType, AtomicInteger> inMap)
	{
		StringBuilder sb = new StringBuilder();
		String header = drawTableHeader();
		String body = drawTableBody(outMap, inMap);
		sb.append(header);
		sb.append(body);
		return sb.toString();
	}

	private static String drawTableHeader()
	{
		StringBuilder sb = new StringBuilder();
		String line = drawLine(((COLUMN_WIDTH * 3) / 2 - OUTGOING_COUNTERS.length()) / 2 - PIPE.length(), 0, 0);
		sb.append(PIPE).append(line).append(OUTGOING_COUNTERS).append(line).append(PIPE);
		line = drawLine(((COLUMN_WIDTH * 3) / 2 - INCOMING_COUNTERS.length()) / 2 - PIPE.length(), 0, 0);
		sb.append(line).append(INCOMING_COUNTERS).append(line).append(SPACE).append(PIPE).append(NEW_LINE);
		return sb.toString();
	}

	private static String drawTableBody(Map<CommandType, AtomicInteger> outMap, Map<CommandType, AtomicInteger> inMap)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(drawTableLine(Arrays.asList(COUNTER_NAME, COUNTER_VALUE, COUNTER_NAME, COUNTER_VALUE))).append(PIPE);
		sb.append(NEW_LINE);
		List<TableRow> rows = translateCommandCounters(outMap, inMap);
		for (TableRow row : rows)
			sb.append(drawTableLine(row.getColumns())).append(PIPE).append(NEW_LINE);
		return sb.toString();
	}

	private static List<TableRow> translateCommandCounters(Map<CommandType, AtomicInteger> out, Map<CommandType, AtomicInteger> in)
	{
		List<TableRow> rows = new ArrayList<>();
		TableRow row = translateCountersRow(CommandType.CONNECT, CommandType.CONNACK, out, in);
		rows.add(row);
		row = translateCountersRow(CommandType.SUBSCRIBE, CommandType.SUBACK, out, in);
		rows.add(row);
		row = translateCountersRow(CommandType.UNSUBSCRIBE, CommandType.UNSUBACK, out, in);
		rows.add(row);
		row = translateCountersRow(CommandType.PINGREQ, CommandType.PINGRESP, out, in);
		rows.add(row);
		row = translateCountersRow(CommandType.PUBLISH, CommandType.PUBLISH, out, in);
		rows.add(row);
		row = translateCountersRow(CommandType.PUBACK, CommandType.PUBACK, out, in);
		rows.add(row);
		row = translateCountersRow(CommandType.PUBREC, CommandType.PUBREC, out, in);
		rows.add(row);
		row = translateCountersRow(CommandType.PUBREL, CommandType.PUBREL, out, in);
		rows.add(row);
		row = translateCountersRow(CommandType.PUBCOMP, CommandType.PUBCOMP, out, in);
		rows.add(row);
		row = translateCountersRow(CommandType.DISCONNECT, CommandType.DISCONNECT, out, in);
		rows.add(row);
		return rows;
	}

	private static TableRow translateCountersRow(CommandType out, CommandType in, Map<CommandType, AtomicInteger> outMap, Map<CommandType, AtomicInteger> inMap)
	{
		TableRow row = null;
		AtomicInteger outCount = outMap.remove(out);
		if (outCount != null)
		{
			String outValue = String.valueOf(outCount.get());
			AtomicInteger inCount = inMap.remove(in);
			String inValue = "0";
			if (inCount != null)
				inValue = String.valueOf(inCount.get());
			row = TableRow.forColumns(out.toString(), outValue, in.toString(), inValue);
		}
		else
		{
			AtomicInteger inCount = inMap.remove(out);
			if (inCount != null)
			{
				String inValue = String.valueOf(inCount.get());
				row = TableRow.forColumns(out.toString(), "0", in.toString(), inValue);
			}
		}

		return row;
	}

	private static String drawTableLine(List<String> columns)
	{
		StringBuilder sb = new StringBuilder();
		for (String column : columns)
			sb.append(drawTableColumn(column));
		String tableLine = sb.toString();
		return trimContentLine(tableLine, SPACE, COLUMN_WIDTH * 3);
	}

	public static String drawTableColumn(String value)
	{
		StringBuilder sb = new StringBuilder();
		String spaces = drawSpaces(((COLUMN_WIDTH * 3) / 4 - value.length() - PIPE.length()) / 2);
		sb.append(PIPE).append(spaces).append(value).append(spaces);
		String column = sb.toString();
		return trimContentLine(column, SPACE, (COLUMN_WIDTH * 3) / 4);
	}

	private static String drawSummaryHeader(UUID scenarioID, boolean status)
	{
		StringBuilder sb = new StringBuilder();
		String spaces = drawLine(4, 1, 0);
		String statusMessage = status ? RESULT_SUCCESS : RESULT_FAILED;
		int prefix = 0;
		int suffix = 0;
		int contentLength = ID.length() + scenarioID.toString().length() + spaces.length() + statusMessage.length() + prefix + suffix;
		String leftLine = drawLine((SCREEN_WIDTH - contentLength) / 2, 0, suffix);
		String rightLine = drawLine((SCREEN_WIDTH - contentLength) / 2, prefix, 0);
		sb.append(leftLine).append(ID).append(scenarioID);
		sb.append(spaces).append(statusMessage).append(rightLine);
		String header = trimContentLine(sb.toString(), MINUS, SCREEN_WIDTH);
		return header + NEW_LINE;
	}

	private static String drawSummaryTimers(long startTime, long finishTime)
	{
		StringBuilder sb = new StringBuilder();

		String firstLine = drawTimeLine(startTime, START_TIME);
		sb.append(firstLine).append(NEW_LINE);

		String secondLine = null;
		if (finishTime > 0)
			secondLine = drawTimeLine(finishTime, FINISH_TIME);
		else
			secondLine = drawDidntFinish();
		sb.append(secondLine).append(NEW_LINE);

		String thirdLine = drawTimeLine(System.currentTimeMillis(), CURRENT_TIME);
		sb.append(thirdLine).append(NEW_LINE);

		return sb.toString();
	}

	private static String drawTimeLine(long timestamp, String content)
	{
		String dateTime = timestampToDateTime(timestamp);
		String firstColumnSpaces = drawSpaces(COLUMN_WIDTH - content.length() - PIPE.length() * 2);
		String secondColumnSpaces = drawSpaces(COLUMN_WIDTH - dateTime.length() - PIPE.length() * 2 - 1);
		int timestampLength = timestamp == 0 ? 1 : TIMESTAMP_LENGTH;
		String thirdColumnSpaces = drawSpaces(COLUMN_WIDTH - timestampLength - PIPE.length() * 2 - 1);
		StringBuilder sb = new StringBuilder();
		sb.append(PIPE).append(content).append(firstColumnSpaces);
		sb.append(PIPE).append(SPACE).append(dateTime).append(secondColumnSpaces).append(SPACE);
		sb.append(PIPE).append(SPACE).append(timestamp).append(thirdColumnSpaces).append(SPACE).append(PIPE);
		return trimContentLine(sb.toString(), SPACE, COLUMN_WIDTH * 3);
	}

	private static String drawDidntFinish()
	{
		String firstColumnSpaces = drawSpaces(COLUMN_WIDTH - FINISH_TIME.length() - PIPE.length() * 2);
		String secondColumnSpaces = drawSpaces(COLUMN_WIDTH - DIDNT_FINISH.length() - PIPE.length() * 2 - 1);
		String thirdColumnSpaces = drawSpaces(COLUMN_WIDTH - PIPE.length() * 2 - 1);
		StringBuilder sb = new StringBuilder();
		sb.append(PIPE).append(FINISH_TIME).append(firstColumnSpaces);
		sb.append(PIPE).append(SPACE).append(DIDNT_FINISH).append(secondColumnSpaces).append(SPACE);
		sb.append(PIPE).append(SPACE).append(thirdColumnSpaces).append(SPACE).append(PIPE);
		return trimContentLine(sb.toString(), SPACE, COLUMN_WIDTH * 3);
	}

	private static String timestampToDateTime(long timestamp)
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat(DATE_TIME_FORMAT);
		Date now = new Date(timestamp);
		String strDate = sdfDate.format(now);
		return strDate;
	}

	private static String drawSummaryTotals(int totalClients, int totalCommands, int successfulClients, int successfulCommands, int failedClients, int failedCommands, int totalErrors, Counter dupIn, Counter dupOut)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(drawFirstTotalsColumn(TOTAL_CLIENTS, String.valueOf(totalClients)));
		sb.append(drawTotalsColumn(TOTAL_COMMANDS, String.valueOf(totalCommands)));
		sb.append(drawTotalsColumn(TOTAL_ERRORS, String.valueOf(totalErrors)));
		sb.append(NEW_LINE);

		sb.append(drawFirstTotalsColumn(SUCCESSFULY_FINISHED, String.valueOf(successfulClients)));
		sb.append(drawTotalsColumn(SUCCESSFULY_FINISHED, String.valueOf(successfulCommands)));
		sb.append(drawTotalsColumn(DUPLICATES_IN, String.valueOf(dupIn.getCount())));
		sb.append(NEW_LINE);

		sb.append(drawFirstTotalsColumn(FAILED, String.valueOf(failedClients)));
		sb.append(drawTotalsColumn(FAILED, String.valueOf(failedCommands)));
		sb.append(drawTotalsColumn(DUPLICATES_OUT, String.valueOf(dupOut.getCount())));
		sb.append(NEW_LINE);

		return sb.toString();
	}

	private static String drawFirstTotalsColumn(String name, String value)
	{
		StringBuilder first = new StringBuilder();
		String spaces = drawSpaces(COLUMN_WIDTH - name.length() - value.length() - PIPE.length() * 2 - DOTS.length() - SPACE.length());
		first.append(PIPE).append(name).append(DOTS).append(spaces).append(value).append(SPACE).append(PIPE);
		return trimContentLine(first.toString(), SPACE, COLUMN_WIDTH);
	}

	private static String drawTotalsColumn(String name, String value)
	{
		StringBuilder first = new StringBuilder();
		String spaces = drawSpaces(COLUMN_WIDTH - name.length() - value.length() - PIPE.length() - DOTS.length() - SPACE.length());
		first.append(name).append(DOTS).append(spaces).append(value).append(SPACE).append(PIPE);
		return trimContentLine(first.toString(), SPACE, COLUMN_WIDTH);
	}

	private static String trimContentLine(String contentLine, String prefix, int width)
	{
		if (contentLine.length() - width > 1)
			throw new IllegalArgumentException("Content doesn't fit. maxLength:" + width + ", actualLength:" + contentLine.length());

		String trimmed = null;
		int diff = contentLine.length() - width;
		if (diff > 0)
			trimmed = contentLine.substring(0, contentLine.length() - diff - 2);
		else if (diff < 0)
			trimmed = contentLine + prefix;
		else
			trimmed = contentLine;

		return trimmed;
	}

	private static String drawLine(int length, int prefix, int suffix)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < prefix; i++)
			sb.append(SPACE);
		for (int i = 0; i < length; i++)
			sb.append(MINUS);
		for (int i = 0; i < suffix; i++)
			sb.append(SPACE);
		return sb.toString();
	}

	private static String drawSpaces(int length)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++)
			sb.append(SPACE);
		return sb.toString();
	}

	private static String drawTableLine()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(PIPE);
		for (int i = 0; i < COLUMNS; i++)
		{
			for (int j = 0; j < COLUMN_WIDTH - 1; j++)
				sb.append(MINUS);
			sb.append(PLUS);
		}
		sb.deleteCharAt(1);
		sb.deleteCharAt(sb.length() - 1);
		sb.append(PIPE);
		sb.append(NEW_LINE);
		return sb.toString();
	}
}
