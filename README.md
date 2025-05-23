# Invest Future
> 만약에 이때(<strong>IF</strong>) 투자했다면 지금 얼마를 벌었을까? <br/>
>
> 항상 저희는 상상을 하곤 합니다. ~~(이 때 돈 넣었으면 지금 4배는 벌었는데)~~<br/>
> 하지만 현실은 돈이 부족하기 떄문에 투자를 하기 쉽지 않습니다. <br/>
> <br/>
> IF에서는 모의투자를 통해 투자에 대한 경험을 쌓고, 투자에 대한 이해를 높일 수 있습니다. <br/>
 <br/>
- 배포 URL : https://investfuture.my  
<br/><br/>

## 다른 모의투자 서비스와의 차이점
 많은 모의투자서비스는 3가지 유형이 있습니다.
 1. 턴제 모의투자
 2. 실시간 모의투자 (거래가 차트에 반영되지 않음)
 3. 자체 시장 모의투자 (거래가 차트에 반영 됨)
  <br/>

<table>
  <thead>
    <tr>
      <td align="center"><img width="350" alt="Image" src="https://github.com/user-attachments/assets/5d346565-4660-467b-8df3-e8112b2e0c9c" /></td>
      <td align="center"><img width="350" alt="Image" src="https://github.com/user-attachments/assets/466f8d9c-253a-40a3-afad-e46cddbcbdc8" /></td>
      <td align="center"><img width="350" alt="Image" src="https://github.com/user-attachments/assets/d116963b-423b-4aee-a416-81e0d479d4c8" /></td>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th align="center"><strong>시장 개입 여부</strong></th>
      <th align="center"><strong>DB 지원 여부</strong></th>
      <th align="center"><strong>봇 지원 여부</strong></th>
    </tr>
  </tbody>
</table>


기존 모의투자 서비스는 실제 거래소의 시세를 지원하면 사용자의 투자 행동이 차트에 반영되지 않거나,<br/>
차트에 반영되지만 실제 시장과는 다른 자체 시장을 형성하는 서비스가 제공되고 있습니다.

 Invest Future(<strong>IF</strong>)는 실제 시장의 추세를 따라가고, 사용자의 투자 행동이 차트에 반영되는 모의투자 서비스입니다.


<br/>
<br/>


## How it works?

> 기존 실시간 모의투자 서비스에서 사용자의 투자 행동이 차트에 반영되지 않는 이유는 사용자의 투자 행동이 차트에 반영될경우 시간이 지날수록 실제 시장의 차트와 모의투자 서비스의 차트의 괴리가 커지기 때문입니다. <br/>
> 비유하자면 시간이 지날수록 멀티버스가 발생합니다.

<img width="1348" alt="Image" src="https://github.com/user-attachments/assets/a850a73f-b77e-4765-877b-fb31782ad22c" />
Invest Future(<strong>IF</strong>)는 사용자의 투자 행동으로 서비스의 차트와 실제 시장의 차트의 괴리가 커지면 <u>자체 매수봇과 매도봇이 매수 매도를 하면서 <strong>실제 시장과의 차이를 보간</strong></u>합니다.




## 프로젝트 특징

<img width="2903" alt="Image" src="https://github.com/user-attachments/assets/d74aea6f-c605-48b6-b77b-42cb9be8565c" />

### 세부 로직
<details>
<summary><b> 주문/체결</b></summary>
<blockquote>
	<img width="1400" alt="Image" src="https://github.com/user-attachments/assets/8e604703-8bb3-43b3-9d2a-e72ab53a3e6b" />
	<p dir="auto"><strong>설명</strong> :  </p>
</blockquote>
</details>

<details>
<summary><b> 보간 </b></summary>
<blockquote>
	<img width="1200" alt="Image" src="https://github.com/user-attachments/assets/56e01e3a-a878-4921-ae40-692845d7989f" />
	<p dir="auto"><strong>설명</strong> :  </p>
</blockquote>
</details>

<details>
<summary><b> 차트/DB </b></summary>
<blockquote>
	<img width="1500" alt="Image" src="https://github.com/user-attachments/assets/3a1850c1-54c9-46cc-b99d-dc0fbf59cd36" />
	<p dir="auto"><strong>설명</strong> :  </p>
</blockquote>
</details>



## 1차 구현 [5.2 ~ 5.15]

코인 한 개(트럼프 코인)을 매수 할 수 있습니다.
사용자의 주문에 따라 그래프차트와 호가창, 실시간 체결창에 반영됩니다.

https://github.com/user-attachments/assets/779b77f1-e778-4a53-b37f-d79a2dc187e8
