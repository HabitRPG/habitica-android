package com.magicmicky.habitrpgwrapper.lib.models.tasks;


/**
 * A reward. Contain a reward that you can see on the website
 * @author MagicMicky
 *
 */
public class Reward extends HabitItem{
	private final HabitType type = HabitType.reward;
	/**
	 * Create a new Reward
	 * @param id the id of the habit
	 * @param notes the notes associated to a habit
	 * @param priority the priority of the habit
	 * @param text the text of the habit
	 * @param value the value (points) of the habit
	 */
	public Reward(String id, String notes, Float priority, String text,
			double value) {
		super(id, notes, priority, text, value);
	}

	public Reward() {
		super();
	}

	@Override
	protected HabitType getType() {
		return type;
	}

	public static class SpecialReward extends Reward {
		private static SpecialReward[] weapons= {
			    new SpecialReward(0, "Training Sword","weapon_0","Training weapon.",0,0),
			    new SpecialReward(1, "Sword","weapon_1","Increases experience gain by 3%.",3,20),
			    new SpecialReward(2, "Axe", "weapon_2","Increases experience gain by 6%.",6,30),
			    new SpecialReward(3, "Morningstar", "weapon_3","Increases experience gain by 9%.",9,45),
			    new SpecialReward(4, "Blue Sword", "weapon_4","Increases experience gain by 12%.",12,65),
			    new SpecialReward(5, "Red Sword", "weapon_5","Increases experience gain by 15%.",15,90),
			    new SpecialReward(6, "Golden Sword", "weapon_6","Increases experience gain by 18%.",18,120),
			    new SpecialReward(7, "Dark Souls Blade", "weapon_7","Increases experience gain by 21%.",21,150)
			  };
		private static SpecialReward[] armors={
			    new SpecialReward(0, "Cloth Armor","armor_0","Training armor.",0,0),
			    new SpecialReward(1, "Leather Armor","armor_1","Decreases HP loss by 4%.",4,30),
			    new SpecialReward(2, "Chain Mail","armor_2","Decreases HP loss by 6%.",6,45),
			    new SpecialReward(3, "Plate Mail","armor_3","Decreases HP loss by 7%.",7,65),
			    new SpecialReward(4, "Red Armor","armor_4","Decreases HP loss by 8%.",8,90),
			    new SpecialReward(5, "Golden Armor","armor_5","Decreases HP loss by 10%.",10,120),
			    new SpecialReward(6, "Shade Armor","armor_6","Decreases HP loss by 12%.",12,150)
			  };//
		private static SpecialReward[] heads = {
			    new SpecialReward(0, "No Helm","head_0","Training helm.",0,0),
			    new SpecialReward(1, "Leather Helm","head_1","Decreases HP loss by 2%.",2,15),
			    new SpecialReward(2, "Chain Coif","head_2","Decreases HP loss by 3%.",3,25),
			    new SpecialReward(3, "Plate Helm","head_3","Decreases HP loss by 4%.",4,45),
			    new SpecialReward(4, "Red Helm","head_4","Decreases HP loss by 5%.",5,60),
			    new SpecialReward(5, "Golden Helm","head_5","Decreases HP loss by 6%.",6,80),
			    new SpecialReward(6, "Shade Helm","head_6","Decreases HP loss by 7%.",7,100)
			  };//			   

		private static SpecialReward[] shields= {
			    new SpecialReward(0, "No Shield","shield_0","No Shield.",0,0),
			    new SpecialReward(1, "Wooden Shield","shield_1","Decreases HP loss by 3%",3,20),
			    new SpecialReward(2, "Buckler","shield_2","Decreases HP loss by 4%.",4,35),
			    new SpecialReward(3, "Reinforced Shield","shield_3","Decreases HP loss by 5%.",5,55),
			    new SpecialReward(4, "Red Shield","shield_4","Decreases HP loss by 7%.",7,70),
			    new SpecialReward(5, "Golden Shield","shield_5","Decreases HP loss by 8%.",8,90),
			    new SpecialReward(6, "Tormented Skull","shield_6","Decreases HP loss by 9%.",9,120)
			  };//			    

			private String classes;
			private int plusValue;
			private SpecialReward currentReward;
			private String type;
			private int level;
		private SpecialReward(int id, String text, String classes, String notes, int plusValue, double value) {
			this.setId(id+"");
			this.setText(text);
			this.setNotes(notes);
			this.setValue(value);
			this.setClasses(classes);
			this.setPlusValue(plusValue);
		}
		private SpecialReward(SpecialReward rew) {
			this(Integer.parseInt(rew.getId()),rew.getText(),rew.getClasses(),rew.getNotes(),rew.getPlusValue(),rew.getValue());
		}
		public SpecialReward(int level, String type) throws ArrayIndexOutOfBoundsException {
			this(getRewardFromLevelAndType(level,type));

			this.type=type;
			this.level = level;
		}
		private static SpecialReward getRewardFromLevelAndType(int level,
				String type) {
			if(type.equals("armor")) {
				if(level > 7)
					level=7;
				return armors[level];
			}
			else if(type.equals("weapon")) {
				if(level > 6)
					level=6;
				return weapons[level];
			}
			else if(type.equals("head")) {
				if(level > 6)
					level=6;
				return heads[level];
			}
			else {
				if(level > 6)
					level=6;
				return shields[level];
			}
		}
		public int getPlusValue() {
			return this.plusValue;
		}
		public void setPlusValue(int plusValue) {
			this.plusValue=plusValue;
		}
		public String getClasses() {
			return this.classes;
		}
		public void setClasses(String classes) {
			this.classes=classes;
		}
		public String getPlusValueType() {
			if(type == "weapon") {
				return "strength";
			}
			return "defense";
		}
		public int getLevel() {
			return level;
		}
	}
}
