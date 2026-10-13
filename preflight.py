import json,pathlib
root=pathlib.Path(__file__).parent
data=json.loads((root/'sheets/design.json').read_text())
errors=[]
for table,rows in data.items():
 keys=set(rows[0])
 ids=set()
 for row in rows:
  if set(row)!=keys: errors.append(f'{table}/{row.get("id")}: inconsistent columns')
  if row['id'] in ids: errors.append(f'{table}: duplicate id {row["id"]}')
  ids.add(row['id'])
  for col in keys:
   if row.get(col) is None or row.get(col)=='': errors.append(f'{table}/{row["id"]}/{col}: unfilled')
assets={r['id'] for r in data['assets']};systems={r['id'] for r in data['systems']+data['heatSystems']}
for row in data['enemies']:
 if row['portrait'] not in assets: errors.append(f'enemy {row["id"]}: unresolved portrait')
 if row['defense'] not in ('left','heat','camera'): errors.append(f'enemy {row["id"]}: unknown defense')
for row in data['hooks']:
 if row['system'] not in systems: errors.append(f'hook {row["id"]}: unresolved system')
if errors: raise SystemExit('\n'.join(errors))
out=root/'src/main/java/nightshift/Design.java';out.parent.mkdir(parents=True,exist_ok=True)
lines=['package nightshift;','public final class Design {']
for table,rows in data.items():
 for row in rows:
  fields=[];values=[]
  for k,v in row.items():
   t='String' if isinstance(v,str) else ('double' if isinstance(v,float) else 'int')
   fields.append(f'{t} {k}');values.append(json.dumps(v))
  name=''.join(x.title() for x in (table+'_'+row['id']).replace('-','_').split('_'))
  lines.append(f' public record {name}('+','.join(fields)+') {}')
  lines.append(f' public static final {name} {table.upper()}_{row["id"].upper().replace("-","_")} = new {name}('+','.join(values)+');')
lines.append('}')
out.write_text('\n'.join(lines))
print('Preflight clean: all cells filled; references resolved; generated one record per sheet row.')
