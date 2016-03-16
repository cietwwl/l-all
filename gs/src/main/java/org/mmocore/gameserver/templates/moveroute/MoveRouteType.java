package org.mmocore.gameserver.templates.moveroute;

/**
 * @author VISTALL
 * @date 22:28/24.10.2011
 */
public enum MoveRouteType
{
	LOOP, // после окончания маршрута сразу же начинает его с начала
	CIRCLE, // после окончания маршрута возвращается по нему же в обратную сторону
	ONCE, // после окончания маршрута удаляется и запускается респавн
	RANDOM // следующая точка маршрута выбирается случайно
}
