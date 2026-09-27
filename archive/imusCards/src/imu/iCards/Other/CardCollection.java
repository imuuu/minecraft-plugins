package imu.iCards.Other;

import java.util.ArrayList;
import java.util.List;

public class CardCollection
{
	private List<Card> cards;

	public CardCollection()
	{
		this.cards = new ArrayList<Card>();
	}

	public void addCard(Card card)
	{
		this.cards.add(card);
	}

	public void removeCard(Card card)
	{
		this.cards.remove(card);
	}

//	public Card findCard(String name)
//	{
//		for (Card card : this.cards)
//		{
//			if (card.getName().equals(name))
//			{
//				return card;
//			}
//		}
//		return null;
//	}
//
//	public boolean unlockCard(String name, int copies)
//	{
//		Card card = findCard(name);
//		if (card != null && card.getCopies() >= copies)
//		{
//			//card.setRarity(card.getRarity() - 1);
//			return true;
//		}
//		return false;
//	}
}
