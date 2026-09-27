package imu.iMiniGames.Interfaces;

import org.bukkit.entity.Player;

import imu.iMiniGames.Handlers.GameCard;
import imu.iMiniGames.Other.MiniGame;

public interface IGameHandeler 
{
	void matchSTART(GameCard gameCard);
	void matchEND(GameCard gameCard, Player winner);
	void afterMatchEnd(GameCard gameCard, Player winner);
	void beforeMatchStart(GameCard gameCard);
	MiniGame afterMatchStart(GameCard gameCard);
	void afterDefaultRequest(Player p, GameCard card);
}
