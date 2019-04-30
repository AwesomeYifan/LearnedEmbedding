from mtree.tests.fixtures.Traces.generator import ADD, REMOVE, QUERY
"""
actions = '20a20r20a20r'
dimensions = 1
remove_chance = 0.1
"""

DIMENSIONS = 1

def PERFORM(callback):
	callback(ADD((85,), QUERY((76,), 54.8648834157218, 1)))
	callback(ADD((23,), QUERY((89,), 74.65340337558084, 7)))
	callback(ADD((4,), QUERY((42,), 57.20409292361404, 1)))
	callback(ADD((89,), QUERY((81,), 24.297905666035547, 8)))
	callback(ADD((65,), QUERY((79,), 58.48945406854628, 5)))
	callback(ADD((99,), QUERY((19,), 3.879144937365302, 2)))
	callback(ADD((28,), QUERY((37,), 15.485927771770056, 9)))
	callback(ADD((58,), QUERY((44,), 29.182524165147974, 3)))
	callback(ADD((21,), QUERY((41,), 0.5963892244128122, 10)))
	callback(ADD((63,), QUERY((65,), 55.452850270885534, 13)))
	callback(ADD((73,), QUERY((69,), 9.677568729663957, 13)))
	callback(ADD((39,), QUERY((4,), 39.574157906668276, 10)))
	callback(ADD((64,), QUERY((48,), 78.40283465920857, 19)))
	callback(ADD((88,), QUERY((62,), 75.63046941583646, 6)))
	callback(ADD((53,), QUERY((36,), 59.44688554287542, 17)))
	callback(ADD((2,), QUERY((84,), 79.82748173270781, 8)))
	callback(ADD((3,), QUERY((22,), 33.20009903454827, 3)))
	callback(ADD((48,), QUERY((56,), 7.740060477037378, 23)))
	callback(ADD((97,), QUERY((87,), 33.37571956205643, 18)))
	callback(ADD((44,), QUERY((47,), 19.641246206561256, 2)))
	callback(REMOVE((48,), QUERY((66,), 33.73692989325793, 16)))
	callback(REMOVE((85,), QUERY((14,), 10.755140514504768, 8)))
	callback(REMOVE((23,), QUERY((92,), 52.22026889640076, 8)))
	callback(REMOVE((21,), QUERY((96,), 77.64930275395733, 15)))
	callback(REMOVE((97,), QUERY((55,), 42.32432748284087, 17)))
	callback(REMOVE((64,), QUERY((24,), 74.37066952931131, 1)))
	callback(REMOVE((4,), QUERY((63,), 12.514063855598314, 11)))
	callback(REMOVE((58,), QUERY((47,), 10.127154562781868, 7)))
	callback(REMOVE((65,), QUERY((14,), 28.193968484978747, 1)))
	callback(REMOVE((63,), QUERY((55,), 35.06708086041131, 2)))
	callback(REMOVE((88,), QUERY((71,), 44.42663412534203, 10)))
	callback(REMOVE((99,), QUERY((80,), 21.1044083123935, 9)))
	callback(REMOVE((3,), QUERY((93,), 49.73965448266873, 1)))
	callback(REMOVE((44,), QUERY((46,), 55.38884734427681, 4)))
	callback(REMOVE((89,), QUERY((16,), 50.6323615100095, 8)))
	callback(REMOVE((39,), QUERY((66,), 25.786597320446525, 6)))
	callback(REMOVE((53,), QUERY((86,), 51.50909972875323, 2)))
	callback(REMOVE((2,), QUERY((69,), 16.53667267570703, 1)))
	callback(REMOVE((73,), QUERY((40,), 41.63405835995741, 5)))
	callback(REMOVE((28,), QUERY((94,), 0.8835770214401784, 2)))
	callback(ADD((88,), QUERY((64,), 58.05567141506366, 1)))
	callback(ADD((77,), QUERY((99,), 30.926090303963043, 7)))
	callback(ADD((31,), QUERY((91,), 62.713033696740254, 8)))
	callback(ADD((62,), QUERY((86,), 3.628130306791393, 8)))
	callback(ADD((9,), QUERY((92,), 32.9857032464313, 11)))
	callback(ADD((38,), QUERY((92,), 7.894094218645078, 4)))
	callback(ADD((35,), QUERY((59,), 64.64797115355819, 4)))
	callback(ADD((86,), QUERY((92,), 22.939012250799273, 2)))
	callback(ADD((25,), QUERY((21,), 62.023717332309474, 8)))
	callback(ADD((64,), QUERY((36,), 59.035993560833745, 13)))
	callback(ADD((80,), QUERY((45,), 11.84942235789225, 16)))
	callback(ADD((58,), QUERY((9,), 16.832641011423295, 16)))
	callback(ADD((5,), QUERY((73,), 24.757634022307737, 7)))
	callback(ADD((83,), QUERY((88,), 22.55302964211884, 8)))
	callback(ADD((34,), QUERY((51,), 52.37534575637638, 1)))
	callback(ADD((0,), QUERY((69,), 2.971374049673736, 2)))
	callback(ADD((69,), QUERY((99,), 51.65665683973704, 5)))
	callback(ADD((82,), QUERY((47,), 73.97599846975403, 25)))
	callback(ADD((53,), QUERY((56,), 47.786777030889425, 4)))
	callback(ADD((45,), QUERY((63,), 43.05203835982154, 20)))
	callback(REMOVE((88,), QUERY((39,), 53.604274072786815, 26)))
	callback(REMOVE((35,), QUERY((46,), 66.13185408000865, 4)))
	callback(REMOVE((80,), QUERY((97,), 55.287734296923, 24)))
	callback(REMOVE((64,), QUERY((56,), 30.09231586734142, 23)))
	callback(REMOVE((53,), QUERY((60,), 15.820065928219194, 12)))
	callback(REMOVE((34,), QUERY((20,), 35.97637628149373, 20)))
	callback(REMOVE((86,), QUERY((81,), 48.562266263645036, 7)))
	callback(REMOVE((69,), QUERY((63,), 17.308024817519687, 14)))
	callback(REMOVE((31,), QUERY((92,), 42.35336157786015, 15)))
	callback(REMOVE((5,), QUERY((19,), 59.30018713019385, 7)))
	callback(REMOVE((83,), QUERY((37,), 78.60878757957363, 1)))
	callback(REMOVE((25,), QUERY((79,), 40.573582507967416, 12)))
	callback(REMOVE((62,), QUERY((37,), 64.95759630027077, 4)))
	callback(REMOVE((58,), QUERY((44,), 71.6957668455346, 4)))
	callback(REMOVE((45,), QUERY((6,), 76.33457659257031, 8)))
	callback(REMOVE((9,), QUERY((50,), 3.201842941083255, 3)))
	callback(REMOVE((82,), QUERY((79,), 75.74990979539582, 8)))
	callback(REMOVE((77,), QUERY((2,), 16.15032887285163, 4)))
	callback(REMOVE((38,), QUERY((54,), 22.543359279533888, 2)))
	callback(REMOVE((0,), QUERY((85,), 56.6924081570469, 4)))
