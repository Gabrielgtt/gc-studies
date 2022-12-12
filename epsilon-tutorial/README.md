# Anotações sobre o estudo do EpsilonGC

Esse estudo segue os passos descritos no tutorial [Do It Yourself (OpenJDK) Garbage Collector](https://shipilev.net/jvm/diy-gc/).

## Tutorial


*TODO* Wendell: Aqui você pode documentar como fazer o tutorial funcionar. Você deve adicionar comandos que precisou rodar, quais seções do tutorial é possível seguir sem problemas, quais partes você teve problemas e como solucionou. O intuito é que qualquer pessoa que leia atualmente consiga entender o que deu errado e rodar para dar certo.


## Usando containers

Se as dependências estiverem muito quebradas, faltando ou em versões conflitantes, pode-se rodar o tutorial dentro de um container Docker. Para isso é preciso instalar as dependências docker na sua máquina local e em seguida seguir o tutorial de dentro do container criado.


### Máquina local

Na sua máquina local você precisa ter o docker instalado e criar o container.

**Instale Docker** ([Link](https://docs.docker.com/engine/install/ubuntu/))
- Dependências
```shell
apt install ca-certificates curl gnupg lsb-release -y
```
- GPK key
```shell
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
```
- Setup do repositório
```shell
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
```
- Installe o docker via apt
```shell
apt update
apt install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y
```


**Crie e entre no modo interativo no container criado**
```shell
docker run --name ubuntu-test -i -t ubuntu:18.04
```

De agora em diante você está no terminal do ubuntu dentro de um container separado.


### Container

**Instale Java**
- Provavelmente isso instalará o java 11.
```shell
apt update
apt install default-jdk
```
